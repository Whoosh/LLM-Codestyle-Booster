package io.github.llmcodestyle.quality;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.github.llmcodestyle.utils.TestCheckSupportUtil.*;
import static org.junit.jupiter.api.Assertions.*;

class TopLevelRecordInPojosPackageCheckTest {

    private static final Map<String, String> NO_PROPS = Map.of();
    private static final int EXPECTED_INVALID_COUNT = 3;
    private static final String CUSTOM_SUFFIX = "dto";

    @Test
    void topLevelRecordsOutsidePojosPackageProduceViolations() throws Exception {
        List<AuditEvent> violations = run("quality/invalid/TopLevelRecordInPojosPackageInvalid.java", NO_PROPS);
        assertEquals(EXPECTED_INVALID_COUNT, violations.size());
        assertTrue(violations.get(0).getMessage().contains("TopLevelRecordInPojosPackageInvalid"));
        assertTrue(violations.get(1).getMessage().contains("LocalFileWriter"));
        assertTrue(violations.get(2).getMessage().contains("ProblemPayload"));
    }

    @Test
    void topLevelRecordInPojosPackageIsValid() throws Exception {
        assertTrue(run("quality/valid/TopLevelRecordInPojosPackageValid.java", NO_PROPS).isEmpty());
    }

    @Test
    void nestedRecordsInNonPojosPackageAreIgnored() throws Exception {
        assertTrue(run("quality/valid/TopLevelRecordInPojosPackageNestedOk.java", NO_PROPS).isEmpty());
    }

    @Test
    void setPackageSuffixAcceptsMatchingPackage() throws Exception {
        new TopLevelRecordInPojosPackageCheck().setPackageSuffix(CUSTOM_SUFFIX);
        assertTrue(run("quality/valid/TopLevelRecordInPojosPackageCustomSuffix.java", Map.of("packageSuffix", CUSTOM_SUFFIX)).isEmpty());
    }

    @Test
    void customPackageSuffixStillFlagsOtherPackages() throws Exception {
        assertEquals(
            EXPECTED_INVALID_COUNT,
            run("quality/invalid/TopLevelRecordInPojosPackageInvalid.java", Map.of("packageSuffix", CUSTOM_SUFFIX)).size(),
            "Even with custom suffix, records in quality.invalid must be flagged");
    }

    private static List<AuditEvent> run(String resource, Map<String, String> props) throws Exception {
        return runTreeWalkerCheck(TopLevelRecordInPojosPackageCheck.class, resource, props);
    }
}
