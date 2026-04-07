package io.github.llmcodestyle.quality;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;
import io.github.llmcodestyle.TestCheckSupport;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestOnlyDelegateCheckTest {

    private static final int EXPECTED_VIOLATIONS = 6;
    private static final String INVALID_FILE = "invalid/TestOnlyDelegateInvalid.java";
    private static final String VALID_FILE = "valid/TestOnlyDelegateValid.java";

    @Test
    void invalidCasesProduceViolations() throws Exception {
        List<AuditEvent> violations = TestCheckSupport.runTreeWalkerCheck(TestOnlyDelegateCheck.class, INVALID_FILE, Map.of());
        assertEquals(EXPECTED_VIOLATIONS, violations.size(), "Expected 6 test-only delegate violations, got " + violations.size());
    }

    @Test
    void validCasesProduceNoViolations() throws Exception {
        assertTrue(TestCheckSupport.runTreeWalkerCheck(TestOnlyDelegateCheck.class, VALID_FILE, Map.of()).isEmpty(), "Expected no violations for valid cases");
    }

    @Test
    void messageContainsBothMethodNames() throws Exception {
        assertTrue(
            TestCheckSupport.runTreeWalkerCheck(
                TestOnlyDelegateCheck.class,
                INVALID_FILE,
                Map.of()).stream().anyMatch(e -> e.getMessage().contains("splitByPattern") && e.getMessage().contains("buildProblems")),
            "Expected both method names in message");
    }

    @Test
    void publicDelegateIsFlagged() throws Exception {
        assertTrue(
            TestCheckSupport.runTreeWalkerCheck(
                TestOnlyDelegateCheck.class,
                INVALID_FILE,
                Map.of()).stream().anyMatch(e -> e.getMessage().contains("publicDelegate")),
            "Public delegate should be flagged");
    }

    @Test
    void instanceDelegateIsFlagged() throws Exception {
        assertTrue(
            TestCheckSupport.runTreeWalkerCheck(
                TestOnlyDelegateCheck.class,
                INVALID_FILE,
                Map.of()).stream().anyMatch(e -> e.getMessage().contains("instanceDelegate")),
            "Instance delegate should be flagged");
    }
}
