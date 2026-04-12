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

    @Test
    void violationLinesArePositive() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(PureSingleUseLocalVariableCheck.class, "simplify/invalid/PureSingleUseVarInvalid.java", Map.of());
        for (AuditEvent v : violations) {
            assertTrue(v.getLine() > 0, "Line should be positive");
            assertFalse(v.getMessage().isEmpty(), "Message should not be empty");
        }
    }

    @Test
    void violationCountIsExact() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(PureSingleUseLocalVariableCheck.class, "simplify/invalid/PureSingleUseVarInvalid.java", Map.of());
        // Exact count assertion kills NEGATE_CONDITIONALS survivors on guards
        assertEquals(EXPECTED_VIOLATIONS, violations.size(),
            "Exact violation count: " + format(violations));
    }

    @Test
    void mutationKillerProducesViolations() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(PureSingleUseLocalVariableCheck.class,
            "simplify/invalid/PureSingleUseVarMutationKiller.java", Map.of());
        // dotMethodCall(len), bareMethodCall(num), varInIfBody(val), varInForBody(val), varInLambda(len)
        assertEquals(5, violations.size(),
            "Mutation killer should produce 5 violations: " + format(violations));
    }

    @Test
    void dotMethodCallExtractMethodName() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(PureSingleUseLocalVariableCheck.class,
            "simplify/invalid/PureSingleUseVarMutationKiller.java", Map.of());
        // dotMethodCall: len = input.length() — extractMethodName line 153 with DOT
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("len")),
            "DOT method call should be detected: " + format(violations));
    }

    @Test
    void bareMethodCallExtractMethodName() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(PureSingleUseLocalVariableCheck.class,
            "simplify/invalid/PureSingleUseVarMutationKiller.java", Map.of());
        // bareMethodCall: num = Integer.parseInt(text) — isPureExpression and extractMethodName with no DOT
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("num")),
            "Bare method call should be detected: " + format(violations));
    }

    @Test
    void flowBlockEligibility() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(PureSingleUseLocalVariableCheck.class,
            "simplify/invalid/PureSingleUseVarMutationKiller.java", Map.of());
        // Variables in if-body and for-body are also eligible (isFlowBlock)
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("val")),
            "Variables in flow blocks should be detected: " + format(violations));
    }

    @Test
    void extractMethodNameAndIsFlowBlock() throws Exception {
        // L158 SURVIVED: `nameIdent != null ? nameIdent.getText() : ""` — extractMethodName with DOT
        // L199 SURVIVED: `isLoopOrCondition(type) || isExceptionBlock(type)` — isFlowBlock
        List<AuditEvent> violations = runTreeWalkerCheck(PureSingleUseLocalVariableCheck.class,
            "simplify/invalid/PureSingleUseMutKill.java", Map.of());
        // dotPureCall: val = input.length() — DOT method call
        // insideIfBlock: inner = text.substring(1) — exercises isFlowBlock for LITERAL_IF
        // insideForLoop: item = text.trim() — exercises isFlowBlock for LITERAL_FOR (loop)
        // insideTryBlock: inner = text.strip() — exercises isFlowBlock for LITERAL_TRY
        assertTrue(violations.size() >= 4,
            "Expected at least 4 violations for flow-block paths: " + format(violations));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("val")),
            "DOT pure call should flag 'val': " + format(violations));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("inner")),
            "If-block or try-block variable should flag 'inner': " + format(violations));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("item")),
            "For-loop variable should flag 'item': " + format(violations));
    }
}
