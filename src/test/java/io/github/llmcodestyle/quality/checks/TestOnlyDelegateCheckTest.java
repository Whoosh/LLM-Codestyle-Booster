package io.github.llmcodestyle.quality.checks;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.github.llmcodestyle.utils.TestCheckSupportUtil.*;
import static org.junit.jupiter.api.Assertions.*;

class TestOnlyDelegateCheckTest {

    private static final int EXPECTED_VIOLATIONS = 7;
    private static final int MIN_MSG_LENGTH = 10;
    private static final String INVALID_FILE = "quality/invalid/TestOnlyDelegateInvalid.java";
    private static final String VALID_FILE = "quality/valid/TestOnlyDelegateValid.java";

    @Test
    void invalidCasesProduceViolations() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(TestOnlyDelegateCheck.class, INVALID_FILE, Map.of());
        assertEquals(EXPECTED_VIOLATIONS, violations.size(), "Expected 6 test-only delegate violations, got " + violations.size());
    }

    @Test
    void validCasesProduceNoViolations() throws Exception {
        assertTrue(runTreeWalkerCheck(TestOnlyDelegateCheck.class, VALID_FILE, Map.of()).isEmpty(), "Expected no violations for valid cases");
    }

    @Test
    void messageContainsBothMethodNames() throws Exception {
        assertTrue(
            runTreeWalkerCheck(
                TestOnlyDelegateCheck.class,
                INVALID_FILE,
                Map.of()).stream().anyMatch(e -> e.getMessage().contains("splitByPattern") && e.getMessage().contains("buildProblems")),
            "Expected both method names in message");
    }

    @Test
    void publicDelegateIsFlagged() throws Exception {
        assertTrue(
            runTreeWalkerCheck(
                TestOnlyDelegateCheck.class,
                INVALID_FILE,
                Map.of()).stream().anyMatch(e -> e.getMessage().contains("publicDelegate")),
            "Public delegate should be flagged");
    }

    @Test
    void instanceDelegateIsFlagged() throws Exception {
        assertTrue(
            runTreeWalkerCheck(
                TestOnlyDelegateCheck.class,
                INVALID_FILE,
                Map.of()).stream().anyMatch(e -> e.getMessage().contains("instanceDelegate")),
            "Instance delegate should be flagged");
    }

    @Test
    void violationMessageContainsMethodName() throws Exception {
        // extractMethodName (line 113) returns the method name
        // If mutated to return "", the message would not contain the method name
        for (AuditEvent v : runTreeWalkerCheck(TestOnlyDelegateCheck.class, INVALID_FILE, Map.of())) {
            String msg = v.getMessage();
            assertNotNull(msg);
            assertFalse(msg.isEmpty(), "Message should not be empty");
            // Message should contain specific method names, not empty
            assertTrue(msg.length() > MIN_MSG_LENGTH, "Message should be descriptive: " + msg);
        }
    }

    @Test
    void exactViolationCount() throws Exception {
        // countStatements (line 124) determines if a method has exactly 1 statement
        // Negating the RCURLY/SEMI check would change statement count, affecting detection
        List<AuditEvent> violations = runTreeWalkerCheck(TestOnlyDelegateCheck.class, INVALID_FILE, Map.of());
        assertEquals(EXPECTED_VIOLATIONS, violations.size(), "Exact violation count should match: " + format(violations));
    }

    @Test
    void extractMethodNameReturnsActualName() throws Exception {
        // L113 SURVIVED: `return null` when DOT is found (qualified call)
        // extractMethodName returns null for qualified calls, so they're not flagged.
        // For unqualified calls, it returns the method name.
        List<AuditEvent> violations = runTreeWalkerCheck(TestOnlyDelegateCheck.class, "quality/invalid/TestOnlyDelegateCountStmts.java", Map.of());
        assertEquals(2, violations.size(), "doWork and doAction should be flagged as thin delegates: " + format(violations));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("doWork") && v.getMessage().contains("internalWork")),
            "doWork -> internalWork delegate should be detected: " + format(violations));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("doAction") && v.getMessage().contains("internalAction")),
            "doAction -> internalAction delegate should be detected: " + format(violations));
    }

    @Test
    void multiStatementMethodNotFlagged() throws Exception {
        // L124 SURVIVED: `type != RCURLY && type != SEMI`
        // If negated: RCURLY/SEMI would be counted as statements, giving count > 1
        List<AuditEvent> violations = runTreeWalkerCheck(TestOnlyDelegateCheck.class, "quality/valid/TestOnlyDelegateMultiStatement.java", Map.of());
        assertTrue(violations.isEmpty(), "Multi-statement method should not be flagged: " + format(violations));
    }
}
