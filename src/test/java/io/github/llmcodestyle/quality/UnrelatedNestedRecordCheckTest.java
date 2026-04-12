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
        List<AuditEvent> violations = run("quality/invalid/UnrelatedNestedRecordInvalid.java");
        assertEquals(EXPECTED_VIOLATIONS, violations.size(), "Expected 7 violations: " + format(violations));
        for (AuditEvent event : violations) {
            assertNotNull(event.getMessage(), "Message should not be null");
            assertFalse(event.getMessage().isEmpty(), "Message should not be empty");
        }
    }

    @Test
    void recordsThatReferenceOuterAreIgnored() throws Exception {
        List<AuditEvent> violations = run("quality/valid/UnrelatedNestedRecordValid.java");
        assertTrue(violations.isEmpty(), "Records referencing outer should not be flagged: " + format(violations));
    }

    @Test
    void topLevelRecordIsIgnored() throws Exception {
        assertTrue(run("quality/valid/UnrelatedNestedRecordTopLevel.java").isEmpty());
    }

    @Test
    void utilClassWithNestedPojoRecordIsFlagged() throws Exception {
        List<AuditEvent> violations = run("quality/invalid/UnrelatedNestedRecordUtilClassInvalid.java");
        assertEquals(1, violations.size(), format(violations));
        assertTrue(violations.get(0).getMessage().contains("ValidationResult"), format(violations));
    }

    private static List<AuditEvent> run(String resource) throws Exception {
        return runTreeWalkerCheck(UnrelatedNestedRecordCheck.class, resource, NO_PROPS);
    }
}
