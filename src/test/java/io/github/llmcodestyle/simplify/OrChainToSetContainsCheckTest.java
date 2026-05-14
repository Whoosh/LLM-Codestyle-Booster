package io.github.llmcodestyle.simplify;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static io.github.llmcodestyle.utils.TestCheckSupportUtil.*;
import static org.junit.jupiter.api.Assertions.*;

class OrChainToSetContainsCheckTest {

    private static final Map<String, String> NO_PROPS = Map.of();
    private static final int EXPECTED_VIOLATIONS = 13;
    private static final int MIN_OPERANDS_LOW = 2;
    private static final Pattern CONTAINS_DIGIT = Pattern.compile(".*\\d+.*");
    private static final int REVERSED_OPERAND_ORDER_LINE = 14;
    private static final int UPPER_SNAKE_LINE = 42;
    private static final int PRIME_LINE = 6;
    private static final int DOTTED_PATH_LINE = 47;
    private static final int METHOD_CALL_LINE = 52;
    private static final int THIS_FIELD_LINE = 58;
    private static final int EQUALS_CHAIN_LINE = 30;

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
        assertFalse(
            runTreeWalkerCheck(OrChainToSetContainsCheck.class, "simplify/valid/OrChainToSetContainsValid.java", Map.of("minOperands", "2")).isEmpty(),
            "minOperands=2 should flag 2-operand chains that are normally skipped");
    }

    @Test
    void setterIsDirectlyInvokable() {
        OrChainToSetContainsCheck check = new OrChainToSetContainsCheck();
        check.setMinOperands(MIN_OPERANDS_LOW);
        assertNotNull(check);
    }

    @Test
    void violationMessageContainsExpressionName() throws Exception {
        List<AuditEvent> violations = run("simplify/invalid/OrChainToSetContainsInvalid.java");
        // isSmallPrime should have 'n' as expression name
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("n")), "Message should contain the LHS expression name: " + format(violations));
    }

    @Test
    void violationMessageContainsOperandCount() throws Exception {
        for (AuditEvent event : run("simplify/invalid/OrChainToSetContainsInvalid.java")) {
            String msg = event.getMessage();
            assertTrue(CONTAINS_DIGIT.matcher(msg).matches(), "Message should contain operand count: " + msg);
        }
    }

    @Test
    void differentLhsDoesNotFire() throws Exception {
        assertTrue(run("simplify/valid/OrChainToSetContainsValid.java").isEmpty(), "Different LHS across operands should not be flagged");
    }

    @Test
    void equalsCallChainIsFlagged() throws Exception {
        List<AuditEvent> violations = run("simplify/invalid/OrChainToSetContainsInvalid.java");
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("name")), "equals chain on 'name' should be flagged: " + format(violations));
    }

    @Test
    void equalsCallWithDifferentReceiversIsNotFlagged() throws Exception {
        assertTrue(run("simplify/valid/OrChainToSetContainsValid.java").isEmpty(), "Different receivers in equals calls should not be flagged");
    }

    @Test
    void reversedOperandOrderIsFlagged() throws Exception {
        List<AuditEvent> violations = run("simplify/invalid/OrChainToSetContainsInvalid.java");
        // reversedOperandOrder: 1 == type || 2 == type || 3 == type should be flagged
        assertTrue(violations.stream().anyMatch(v -> v.getLine() == REVERSED_OPERAND_ORDER_LINE),
            "Reversed operand order (LITERAL == expr) should be flagged on line 14: " + format(violations));
    }

    @Test
    void upperSnakeCaseConstantsAreFlagged() throws Exception {
        List<AuditEvent> violations = run("simplify/invalid/OrChainToSetContainsInvalid.java");
        assertTrue(violations.stream().anyMatch(v -> v.getLine() == UPPER_SNAKE_LINE), "UPPER_SNAKE_CASE constants as RHS should be flagged on line 42: " + format(violations));
    }

    @Test
    void renderExpressionIdentProducesCorrectName() throws Exception {
        List<AuditEvent> violations = run("simplify/invalid/OrChainToSetContainsInvalid.java");
        // isSmallPrime: 'n == 2 || n == 3 || ...' — expression name should be 'n'
        AuditEvent primeViolation = violations.stream()
            .filter(v -> v.getLine() == PRIME_LINE)
            .findFirst()
            .orElse(null);
        assertNotNull(primeViolation, "Expected violation on line 6: " + format(violations));
        assertTrue(primeViolation.getMessage().contains("n"), "Rendered IDENT should be 'n': " + primeViolation.getMessage());
    }

    @Test
    void renderExpressionDotProducesQualifiedName() throws Exception {
        List<AuditEvent> violations = run("simplify/invalid/OrChainToSetContainsInvalid.java");
        // dottedPathLhs: arr.length == 1 || ... — expression should render as 'arr.length'
        AuditEvent dotViolation = violations.stream()
            .filter(v -> v.getLine() == DOTTED_PATH_LINE)
            .findFirst()
            .orElse(null);
        assertNotNull(dotViolation, "Expected violation on line 47: " + format(violations));
        assertTrue(dotViolation.getMessage().contains("arr.length"), "Rendered DOT should be 'arr.length': " + dotViolation.getMessage());
    }

    @Test
    void renderExpressionMethodCallProducesCallSyntax() throws Exception {
        List<AuditEvent> violations = run("simplify/invalid/OrChainToSetContainsInvalid.java");
        // methodCallLhs: s.length() == 1 || ... — expression should render as 's.length()'
        AuditEvent callViolation = violations.stream()
            .filter(v -> v.getLine() == METHOD_CALL_LINE)
            .findFirst()
            .orElse(null);
        assertNotNull(callViolation, "Expected violation on line 52: " + format(violations));
        assertTrue(callViolation.getMessage().contains("s.length()"), "Rendered METHOD_CALL should be 's.length()': " + callViolation.getMessage());
    }

    @Test
    void renderExpressionThisFieldProducesThisQualified() throws Exception {
        List<AuditEvent> violations = run("simplify/invalid/OrChainToSetContainsInvalid.java");
        // thisFieldLhs: this.field == 10 || ... — expression should render as 'this.field'
        AuditEvent thisViolation = violations.stream()
            .filter(v -> v.getLine() == THIS_FIELD_LINE)
            .findFirst()
            .orElse(null);
        assertNotNull(thisViolation, "Expected violation on line 58: " + format(violations));
        assertTrue(thisViolation.getMessage().contains("this.field"), "Rendered LITERAL_THIS path should be 'this.field': " + thisViolation.getMessage());
    }

    @Test
    void detectEqualityChainViolationContainsExpressionName() throws Exception {
        // Each equality chain violation should contain a non-empty expression name
        for (AuditEvent v : run("simplify/invalid/OrChainToSetContainsInvalid.java")) {
            if (!v.getMessage().contains("name")) {
                // Skip equals-chain violations, just check equality chains
                String msg = v.getMessage();
                assertFalse(msg.contains("Set.of().contains(?)"), "Expression name should not be '?' for valid chains: " + msg);
            }
        }
    }

    @Test
    void digitConstantsInUpperSnakeCaseAreFlagged() throws Exception {
        List<AuditEvent> violations = run("simplify/invalid/OrChainUpperSnakeEdge.java");
        assertEquals(1, violations.size(), "Constants with digits should be flagged: " + format(violations));
        assertTrue(violations.get(0).getMessage().contains("type"), "Message should contain 'type': " + violations.get(0).getMessage());
    }

    @Test
    void lowercaseIdentifiersNotTreatedAsConstants() throws Exception {
        List<AuditEvent> violations = run("simplify/valid/OrChainUpperSnakeEdgeValid.java");
        assertTrue(violations.isEmpty(), "Lowercase identifiers should not be treated as UPPER_SNAKE_CASE constants: " + format(violations));
    }

    @Test
    void eachViolationContainsOperandCount() throws Exception {
        for (AuditEvent v : run("simplify/invalid/OrChainToSetContainsInvalid.java")) {
            // The message format includes the operand count
            assertTrue(CONTAINS_DIGIT.matcher(v.getMessage()).matches(), "Should contain count: " + v.getMessage());
        }
    }

    @Test
    void equalsChainReportsReceiverName() throws Exception {
        List<AuditEvent> violations = run("simplify/invalid/OrChainToSetContainsInvalid.java");
        // equalsChainOnString: name.equals("alpha") || ... — should report 'name'
        AuditEvent equalsViolation = violations.stream()
            .filter(v -> v.getLine() == EQUALS_CHAIN_LINE)
            .findFirst()
            .orElse(null);
        assertNotNull(equalsViolation, "Expected violation on line 30: " + format(violations));
        assertTrue(equalsViolation.getMessage().contains("name"), "Equals chain should report receiver 'name': " + equalsViolation.getMessage());
    }

    @Test
    void parenthesizedSubChainsAreFlattened() throws Exception {
        List<AuditEvent> violations = run("simplify/invalid/OrChainParenthesizedSubChain.java");
        assertEquals(3, violations.size(),
            "Three 3-operand chains (right-paren, left-paren, full-wrap) must be flagged: " + format(violations));
    }

    @Test
    void renderExpressionLiteralThisPath() throws Exception {
        // L224 SURVIVED: `node.getType() == LITERAL_THIS`
        // If negated: `this` keyword would fall through to the default getText() path,
        // returning "this" anyway (equivalent mutant), OR producing "?" if getText() is null.
        // L213 NO_COVERAGE: `return "?"` (null node)
        // L227 NO_COVERAGE: `node.getText() == null ? "?" : node.getText()`
        // Exercise renderExpression with this.field chain
        List<AuditEvent> violations = run("simplify/invalid/OrChainRenderExpression.java");
        // this.value == 1 || this.value == 2 || this.value == 3
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("this")), "renderExpression should handle LITERAL_THIS: " + format(violations));
    }

    private static List<AuditEvent> run(String resource) throws Exception {
        return runTreeWalkerCheck(OrChainToSetContainsCheck.class, resource, NO_PROPS);
    }
}
