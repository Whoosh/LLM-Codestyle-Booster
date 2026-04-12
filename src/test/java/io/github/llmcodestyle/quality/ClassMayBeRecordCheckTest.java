package io.github.llmcodestyle.quality;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.github.llmcodestyle.utils.TestCheckSupportUtil.*;
import static org.junit.jupiter.api.Assertions.*;

class ClassMayBeRecordCheckTest {

    private static final Map<String, String> NO_PROPS = Map.of();
    private static final int EXPECTED_VIOLATIONS = 4;

    @Test
    void dataCarrierClassesProduceViolations() throws Exception {
        List<AuditEvent> violations = run("quality/invalid/ClassMayBeRecordInvalid.java");
        assertEquals(EXPECTED_VIOLATIONS, violations.size(), format(violations));
    }

    @Test
    void statsClassIsFlagged() throws Exception {
        List<AuditEvent> violations = run("quality/invalid/ClassMayBeRecordInvalid.java");
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Stats")), format(violations));
    }

    @Test
    void pairClassIsFlagged() throws Exception {
        List<AuditEvent> violations = run("quality/invalid/ClassMayBeRecordInvalid.java");
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Pair")), format(violations));
    }

    @Test
    void classWithStandardMethodsOnlyIsFlagged() throws Exception {
        List<AuditEvent> violations = run("quality/invalid/ClassMayBeRecordInvalid.java");
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Point")), format(violations));
    }

    @Test
    void topLevelDataCarrierIsFlagged() throws Exception {
        List<AuditEvent> violations = run("quality/invalid/ClassMayBeRecordInvalid.java");
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("TopLevelDataCarrier")), format(violations));
    }

    @Test
    void nonDataCarrierClassesAreIgnored() throws Exception {
        assertTrue(run("quality/valid/ClassMayBeRecordValid.java").isEmpty());
    }

    private static List<AuditEvent> run(String resource) throws Exception {
        return runTreeWalkerCheck(ClassMayBeRecordCheck.class, resource, NO_PROPS);
    }
}
