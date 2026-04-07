package io.github.llmcodestyle.simplify;

import io.github.llmcodestyle.TestCheckSupport;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PureSingleUseLocalVariableCheckTest {

    private static final int EXPECTED_VIOLATIONS = 4;

    @Test
    void invalidCasesProduceViolations() throws Exception {
        List<AuditEvent> violations = TestCheckSupport.runTreeWalkerCheck(PureSingleUseLocalVariableCheck.class, "simplify/invalid/PureSingleUseVarInvalid.java", Map.of());
        assertEquals(EXPECTED_VIOLATIONS, violations.size(), "Expected 4 pure single-use violations, got: " + violations.size());
    }

    @Test
    void violationMessagesContainVariableName() throws Exception {
        List<AuditEvent> violations = TestCheckSupport.runTreeWalkerCheck(PureSingleUseLocalVariableCheck.class, "simplify/invalid/PureSingleUseVarInvalid.java", Map.of());
        assertTrue(violations.stream().anyMatch(e -> e.getMessage().contains("number")), "Expected 'number' in messages");
        assertTrue(violations.stream().anyMatch(e -> e.getMessage().contains("len")), "Expected 'len' in messages");
    }

    @Test
    void validCasesProduceNoViolations() throws Exception {
        assertTrue(
            TestCheckSupport.runTreeWalkerCheck(
                PureSingleUseLocalVariableCheck.class, "simplify/valid/PureSingleUseVarValid.java", Map.of()).isEmpty(),
            "Expected no violations");
    }
}
