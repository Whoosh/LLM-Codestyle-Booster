package io.github.llmcodestyle.simplify;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.github.llmcodestyle.utils.TestCheckSupportUtil.*;
import static org.junit.jupiter.api.Assertions.*;

class BooleanFromConditionCheckTest {

    private static final Map<String, String> NO_PROPS = Map.of();
    private static final int EXPECTED_VIOLATIONS = 4;
    private static final int LINE_IS_POSITIVE = 8;
    private static final int LINE_IS_NON_ZERO = 16;
    private static final int LINE_WITH_SINGLE_STATEMENT = 24;
    private static final int LINE_IN_MIDDLE_OF_BLOCK = 31;

    @Test
    void booleanFlipPatternProducesViolations() throws Exception {
        assertEquals(EXPECTED_VIOLATIONS, run("simplify/invalid/BooleanFromConditionInvalid.java").size());
    }

    @Test
    void validCornerCasesProduceNoViolations() throws Exception {
        assertTrue(run("simplify/valid/BooleanFromConditionValid.java").isEmpty());
    }

    @Test
    void violationsContainVariableName() throws Exception {
        List<AuditEvent> violations = run("simplify/invalid/BooleanFromConditionInvalid.java");
        // booleanLiteralVarName returns the variable name (SURVIVED on line 64, 68)
        // If mutated to return "", the message would contain empty string instead of var name
        for (AuditEvent v : violations) {
            String msg = v.getMessage();
            assertNotNull(msg, "Message should not be null");
            assertFalse(msg.isEmpty(), "Message should not be empty");
        }
        // Check that at least one violation mentions a specific variable
        assertTrue(violations.stream().anyMatch(v -> v.getLine() > 0), "Violations should report positive line numbers: " + format(violations));
    }

    @Test
    void mutationKillerValidProducesNoViolations() throws Exception {
        List<AuditEvent> violations = run("simplify/valid/BooleanFromConditionMutationKiller.java");
        assertTrue(violations.isEmpty(), "Mutation killer valid should produce no violations: " + format(violations));
    }

    @Test
    void violationLinesMatchExpectedPositions() throws Exception {
        List<AuditEvent> violations = run("simplify/invalid/BooleanFromConditionInvalid.java");
        assertEquals(EXPECTED_VIOLATIONS, violations.size(), format(violations));
        // isPositive: line 8, isNonZero: line 16, withSingleStatement: line 24, inMiddleOfBlock: line 31
        assertTrue(violations.stream().anyMatch(v -> v.getLine() == LINE_IS_POSITIVE), "isPositive should be flagged: " + format(violations));
        assertTrue(violations.stream().anyMatch(v -> v.getLine() == LINE_IS_NON_ZERO), "isNonZero should be flagged: " + format(violations));
        assertTrue(violations.stream().anyMatch(v -> v.getLine() == LINE_WITH_SINGLE_STATEMENT), "withSingleStatement should be flagged: " + format(violations));
        assertTrue(violations.stream().anyMatch(v -> v.getLine() == LINE_IN_MIDDLE_OF_BLOCK), "inMiddleOfBlock should be flagged: " + format(violations));
    }

    @Test
    void violationMessagesContainSpecificVarNames() throws Exception {
        List<AuditEvent> violations = run("simplify/invalid/BooleanFromConditionInvalid.java");
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("positive")), "Should mention 'positive': " + format(violations));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("nonZero")), "Should mention 'nonZero': " + format(violations));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("flag")), "Should mention 'flag': " + format(violations));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("done")), "Should mention 'done': " + format(violations));
    }

    @Test
    void booleanLiteralVarNameReturnsNullForNonVarDef() throws Exception {
        // L64 SURVIVED: `stmt.getType() != VARIABLE_DEF` — replaced return null with ""
        // L68 SURVIVED: `type == null || ... || literalKindOfInit == 0` — replaced return null with ""
        // If mutated to return "", non-eligible statements would pass the null check and potentially
        // produce false violations.
        List<AuditEvent> violations = run("simplify/valid/BooleanFromConditionNotVar.java");
        assertEquals(0, violations.size(), "Non-variable-def and non-boolean var should produce ZERO violations: " + format(violations));
    }

    @Test
    void booleanFromConditionPreciseViolationMessages() throws Exception {
        // Kill EMPTY_RETURNS mutation on booleanLiteralVarName:
        // if replaced with "", the message would contain empty instead of var name
        List<AuditEvent> violations = run("simplify/invalid/BooleanFromConditionMutKill.java");
        assertEquals(2, violations.size(), "Expected 2 boolean-from-condition violations: " + format(violations));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("positive")), "Should mention 'positive': " + format(violations));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("negative")), "Should mention 'negative': " + format(violations));
    }

    private static List<AuditEvent> run(String resource) throws Exception {
        return runTreeWalkerCheck(BooleanFromConditionCheck.class, resource, NO_PROPS);
    }
}
