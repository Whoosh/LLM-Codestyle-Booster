package io.github.llmcodestyle.simplify;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.github.llmcodestyle.utils.TestCheckSupportUtil.*;
import static org.junit.jupiter.api.Assertions.*;

class SingleUseLocalVariableCheckTest {

    private static final int EXPECTED_VIOLATIONS = 9;

    @Test
    void invalidCasesProduceViolations() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(SingleUseLocalVariableCheck.class, "simplify/invalid/SingleUseVarInvalid.java", Map.of());
        assertEquals(EXPECTED_VIOLATIONS, violations.size(), "Expected 9 single-use variable violations, got: " + violations.size());
    }

    @Test
    void violationMessagesContainVariableName() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(SingleUseLocalVariableCheck.class, "simplify/invalid/SingleUseVarInvalid.java", Map.of());
        assertTrue(violations.stream().anyMatch(e -> e.getMessage().contains("fixed")), "Expected 'fixed' in violation messages");
        assertTrue(violations.stream().anyMatch(e -> e.getMessage().contains("trimmed")), "Expected 'trimmed' in violation messages");
    }

    @Test
    void validCasesProduceNoViolations() throws Exception {
        assertTrue(runTreeWalkerCheck(SingleUseLocalVariableCheck.class, "simplify/valid/SingleUseVarValid.java", Map.of()).isEmpty(), "Expected no violations");
    }
}
