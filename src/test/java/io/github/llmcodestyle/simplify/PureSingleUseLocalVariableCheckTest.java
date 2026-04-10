package io.github.llmcodestyle.simplify;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.github.llmcodestyle.utils.TestCheckSupportUtil.*;
import static org.junit.jupiter.api.Assertions.*;

class PureSingleUseLocalVariableCheckTest {

    private static final int EXPECTED_VIOLATIONS = 4;

    @Test
    void invalidCasesProduceViolations() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(PureSingleUseLocalVariableCheck.class, "simplify/invalid/PureSingleUseVarInvalid.java", Map.of());
        assertEquals(EXPECTED_VIOLATIONS, violations.size(), "Expected 4 pure single-use violations, got: " + violations.size());
    }

    @Test
    void violationMessagesContainVariableName() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(PureSingleUseLocalVariableCheck.class, "simplify/invalid/PureSingleUseVarInvalid.java", Map.of());
        assertTrue(violations.stream().anyMatch(e -> e.getMessage().contains("number")), "Expected 'number' in messages");
        assertTrue(violations.stream().anyMatch(e -> e.getMessage().contains("len")), "Expected 'len' in messages");
    }

    @Test
    void validCasesProduceNoViolations() throws Exception {
        assertTrue(runTreeWalkerCheck(PureSingleUseLocalVariableCheck.class, "simplify/valid/PureSingleUseVarValid.java", Map.of()).isEmpty(), "Expected no violations");
    }
}
