package io.github.llmcodestyle.quality;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.github.llmcodestyle.utils.TestCheckSupportUtil.*;
import static org.junit.jupiter.api.Assertions.*;

class UnrelatedNestedRecordCheckTest {

    private static final Map<String, String> NO_PROPS = Map.of();
    private static final int EXPECTED_VIOLATIONS = 7;

    @Test
    void unrelatedNestedRecordsProduceViolations() throws Exception {
        assertEquals(EXPECTED_VIOLATIONS, run("quality/invalid/UnrelatedNestedRecordInvalid.java").size());
    }

    @Test
    void recordsThatReferenceOuterAreIgnored() throws Exception {
        assertTrue(run("quality/valid/UnrelatedNestedRecordValid.java").isEmpty());
    }

    @Test
    void topLevelRecordIsIgnored() throws Exception {
        assertTrue(run("quality/valid/UnrelatedNestedRecordTopLevel.java").isEmpty());
    }

    private static List<AuditEvent> run(String resource) throws Exception {
        return runTreeWalkerCheck(UnrelatedNestedRecordCheck.class, resource, NO_PROPS);
    }
}
