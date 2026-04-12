package io.github.llmcodestyle.layout;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.github.llmcodestyle.utils.TestCheckSupportUtil.*;
import static org.junit.jupiter.api.Assertions.*;

class MethodCallArgumentsOnSameLineCheckTest {

    private static final int EXPECTED_VIOLATIONS = 2;
    private static final Map<String, String> NO_PROPS = Map.of();

    @Test
    void invalidCasesProduceViolations() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(MethodCallArgumentsOnSameLineCheck.class, "layout/invalid/MethodCallArgsInvalid.java", NO_PROPS);
        assertEquals(EXPECTED_VIOLATIONS, violations.size(), "Expected 2 mixed-argument violations, got: " + format(violations));
    }

    @Test
    void validCasesProduceNoViolations() throws Exception {
        assertTrue(runTreeWalkerCheck(MethodCallArgumentsOnSameLineCheck.class, "layout/valid/MethodCallArgsValid.java", NO_PROPS).isEmpty(), "Expected no violations");
    }

    @Test
    void violationsReportCorrectLines() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(MethodCallArgumentsOnSameLineCheck.class, "layout/invalid/MethodCallArgsInvalid.java", NO_PROPS);
        assertEquals(EXPECTED_VIOLATIONS, violations.size());
        for (AuditEvent event : violations) {
            assertTrue(event.getLine() > 0, "Violation should report a positive line number");
            assertNotNull(event.getMessage(), "Message should not be null");
            assertFalse(event.getMessage().isEmpty(), "Message should not be empty");
        }
    }

    @Test
    void allArgsOnOneLineIsNotFlagged() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(MethodCallArgumentsOnSameLineCheck.class, "layout/valid/MethodCallArgsValid.java", NO_PROPS);
        assertTrue(violations.isEmpty(), "All args on one line should not be flagged: " + format(violations));
    }

    @Test
    void oneArgPerLineIsNotFlagged() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(MethodCallArgumentsOnSameLineCheck.class, "layout/valid/MethodCallArgsValid.java", NO_PROPS);
        assertTrue(violations.isEmpty(), "One arg per line should not be flagged: " + format(violations));
    }
}
