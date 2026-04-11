package io.github.llmcodestyle.quality;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.github.llmcodestyle.utils.TestCheckSupportUtil.*;
import static org.junit.jupiter.api.Assertions.*;

class TopLevelEnumInEnumsPackageCheckTest {

    private static final Map<String, String> NO_PROPS = Map.of();
    private static final int EXPECTED_INVALID_COUNT = 3;
    private static final String CUSTOM_SUFFIX = "values";

    @Test
    void topLevelEnumsOutsideEnumsPackageProduceViolations() throws Exception {
        List<AuditEvent> violations = run("quality/invalid/TopLevelEnumInEnumsPackageInvalid.java", NO_PROPS);
        assertEquals(EXPECTED_INVALID_COUNT, violations.size());
        assertTrue(violations.get(0).getMessage().contains("TopLevelEnumInEnumsPackageInvalid"));
        assertTrue(violations.get(1).getMessage().contains("HttpStatus"));
        assertTrue(violations.get(2).getMessage().contains("Direction"));
    }

    @Test
    void topLevelEnumInEnumsPackageIsValid() throws Exception {
        assertTrue(run("quality/valid/TopLevelEnumInEnumsPackageValid.java", NO_PROPS).isEmpty());
    }

    @Test
    void nestedEnumsInNonEnumsPackageAreIgnored() throws Exception {
        assertTrue(run("quality/valid/TopLevelEnumInEnumsPackageNestedOk.java", NO_PROPS).isEmpty());
    }

    @Test
    void setPackageSuffixAcceptsMatchingPackage() throws Exception {
        new TopLevelEnumInEnumsPackageCheck().setPackageSuffix(CUSTOM_SUFFIX);
        assertTrue(run("quality/valid/TopLevelEnumInEnumsPackageCustomSuffix.java", Map.of("packageSuffix", CUSTOM_SUFFIX)).isEmpty());
    }

    @Test
    void customPackageSuffixStillFlagsOtherPackages() throws Exception {
        assertEquals(
            EXPECTED_INVALID_COUNT,
            run("quality/invalid/TopLevelEnumInEnumsPackageInvalid.java", Map.of("packageSuffix", CUSTOM_SUFFIX)).size(),
            "Even with custom suffix, enums in quality.invalid must be flagged");
    }

    private static List<AuditEvent> run(String resource, Map<String, String> props) throws Exception {
        return runTreeWalkerCheck(TopLevelEnumInEnumsPackageCheck.class, resource, props);
    }
}
