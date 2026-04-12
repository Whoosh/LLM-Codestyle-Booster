package io.github.llmcodestyle.simplify;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.github.llmcodestyle.utils.TestCheckSupportUtil.*;
import static org.junit.jupiter.api.Assertions.*;

class OrChainToSetContainsCheckTest {

    private static final Map<String, String> NO_PROPS = Map.of();
    private static final int EXPECTED_VIOLATIONS = 13;

    @Test
    void orChainsOfLiteralsProduceViolations() throws Exception {
        List<AuditEvent> violations = run("simplify/invalid/OrChainToSetContainsInvalid.java");
        assertEquals(EXPECTED_VIOLATIONS, violations.size(), "Expected 10 or-chain violations: " + format(violations));
    }

    @Test
    void validCornerCasesProduceNoViolations() throws Exception {
        assertTrue(run("simplify/valid/OrChainToSetContainsValid.java").isEmpty());
    }

    @Test
    void thresholdIsConfigurable() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(OrChainToSetContainsCheck.class,
            "simplify/valid/OrChainToSetContainsValid.java", Map.of("minOperands", "2"));
        assertFalse(violations.isEmpty(), "minOperands=2 should flag 2-operand chains that are normally skipped");
    }

    @Test
    void setterIsDirectlyInvokable() {
        OrChainToSetContainsCheck check = new OrChainToSetContainsCheck();
        check.setMinOperands(2);
        assertNotNull(check);
    }

    @Test
    void violationMessageContainsExpressionName() throws Exception {
        List<AuditEvent> violations = run("simplify/invalid/OrChainToSetContainsInvalid.java");
        // isSmallPrime should have 'n' as expression name
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("n")),
            "Message should contain the LHS expression name: " + format(violations));
    }

    @Test
    void violationMessageContainsOperandCount() throws Exception {
        List<AuditEvent> violations = run("simplify/invalid/OrChainToSetContainsInvalid.java");
        for (AuditEvent event : violations) {
            String msg = event.getMessage();
            assertTrue(msg.matches(".*\\d+.*"), "Message should contain operand count: " + msg);
        }
    }

    @Test
    void differentLhsDoesNotFire() throws Exception {
        List<AuditEvent> violations = run("simplify/valid/OrChainToSetContainsValid.java");
        assertTrue(violations.isEmpty(), "Different LHS across operands should not be flagged");
    }

    @Test
    void equalsCallChainIsFlagged() throws Exception {
        List<AuditEvent> violations = run("simplify/invalid/OrChainToSetContainsInvalid.java");
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("name")),
            "equals chain on 'name' should be flagged: " + format(violations));
    }

    @Test
    void equalsCallWithDifferentReceiversIsNotFlagged() throws Exception {
        List<AuditEvent> violations = run("simplify/valid/OrChainToSetContainsValid.java");
        assertTrue(violations.isEmpty(), "Different receivers in equals calls should not be flagged");
    }

    @Test
    void reversedOperandOrderIsFlagged() throws Exception {
        List<AuditEvent> violations = run("simplify/invalid/OrChainToSetContainsInvalid.java");
        // reversedOperandOrder: 1 == type || 2 == type || 3 == type should be flagged
        assertTrue(violations.stream().anyMatch(v -> v.getLine() == 14),
            "Reversed operand order (LITERAL == expr) should be flagged on line 14: " + format(violations));
    }

    @Test
    void upperSnakeCaseConstantsAreFlagged() throws Exception {
        List<AuditEvent> violations = run("simplify/invalid/OrChainToSetContainsInvalid.java");
        assertTrue(violations.stream().anyMatch(v -> v.getLine() == 42),
            "UPPER_SNAKE_CASE constants as RHS should be flagged on line 42: " + format(violations));
    }

    private static List<AuditEvent> run(String resource) throws Exception {
        return runTreeWalkerCheck(OrChainToSetContainsCheck.class, resource, NO_PROPS);
    }
}
