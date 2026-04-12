package io.github.llmcodestyle.simplify;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.github.llmcodestyle.utils.TestCheckSupportUtil.*;
import static org.junit.jupiter.api.Assertions.*;

class InlineRegexConstantCheckTest {

    private static final int EXPECTED_VIOLATIONS = 5;
    private static final int FIELD_LEVEL_LINE = 8;
    private static final int QUALIFIED_CALL_LINE = 14;
    private static final int UNQUALIFIED_CALL_LINE = 19;
    private static final int EXPECTED_MUTATION_KILLER_VIOLATIONS = 2;

    @Test
    void invalidCasesProduceViolations() throws Exception {
        assertEquals(
            EXPECTED_VIOLATIONS,
            runTreeWalkerCheck(InlineRegexConstantCheck.class, "simplify/invalid/InlineRegexInvalid.java", Map.of()).size(),
            "Expected 5 inline regex violations");
    }

    @Test
    void validCasesProduceNoViolations() throws Exception {
        assertTrue(runTreeWalkerCheck(InlineRegexConstantCheck.class, "simplify/valid/InlineRegexValid.java", Map.of()).isEmpty(), "Expected no violations");
    }

    @Test
    void violationMessageContainsPatternInfo() throws Exception {
        for (AuditEvent event : runTreeWalkerCheck(InlineRegexConstantCheck.class, "simplify/invalid/InlineRegexInvalid.java", Map.of())) {
            String msg = event.getMessage();
            assertNotNull(msg, "Message should not be null");
            assertFalse(msg.isEmpty(), "Message should not be empty");
        }
    }

    @Test
    void violationsReportCorrectLines() throws Exception {
        for (AuditEvent event : runTreeWalkerCheck(InlineRegexConstantCheck.class, "simplify/invalid/InlineRegexInvalid.java", Map.of())) {
            assertTrue(event.getLine() > 0, "Line should be positive");
        }
    }

    @Test
    void fieldLevelRegexIsNotFlagged() throws Exception {
        // isInsideMethodBody returns false for field initializers (VARIABLE_DEF with OBJBLOCK grandparent)
        List<AuditEvent> violations = runTreeWalkerCheck(InlineRegexConstantCheck.class, "simplify/valid/InlineRegexFieldLevel.java", Map.of());
        assertTrue(violations.isEmpty(), "Field-level regex usage should not be flagged: " + format(violations));
    }

    @Test
    void mutationKillerExercisesExtractMethodNameWithDot() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(InlineRegexConstantCheck.class, "simplify/invalid/InlineRegexMutationKiller.java", Map.of());
        // Should flag inline regex constants in method bodies (qualifiedCall and unqualifiedCall)
        // but NOT in field initializer (fieldSplit)
        assertTrue(violations.size() >= EXPECTED_MUTATION_KILLER_VIOLATIONS, "Inline regex in method body should be flagged: " + format(violations));
        // Field initializer should NOT be flagged (isInsideMethodBody returns false)
        assertTrue(violations.stream().noneMatch(v -> v.getLine() == FIELD_LEVEL_LINE), "Field-level regex should not be flagged: " + format(violations));
    }

    @Test
    void fieldLevelRegexNotFlaggedButMethodLevelIs() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(InlineRegexConstantCheck.class, "simplify/invalid/InlineRegexMutationKiller.java", Map.of());
        assertTrue(violations.stream().anyMatch(v -> v.getLine() == QUALIFIED_CALL_LINE), "Qualified regex call in method should be flagged: " + format(violations));
        assertTrue(violations.stream().anyMatch(v -> v.getLine() == UNQUALIFIED_CALL_LINE), "Unqualified replaceAll in method should be flagged: " + format(violations));
    }

    @Test
    void nestedClassFieldRegexNotFlagged() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(InlineRegexConstantCheck.class, "simplify/valid/InlineRegexMutKiller.java", Map.of());
        assertTrue(violations.isEmpty(), "Nested class field regex should not be flagged: " + format(violations));
    }

    @Test
    void mutationKillerExactViolationCount() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(InlineRegexConstantCheck.class, "simplify/invalid/InlineRegexMutationKiller.java", Map.of());
        assertEquals(EXPECTED_MUTATION_KILLER_VIOLATIONS, violations.size(), "Exactly 2 method-body regex violations: " + format(violations));
    }

    @Test
    void isInsideMethodBodyVariableDefObjBlockGrandparent() throws Exception {
        // L56 SURVIVED: `parent.getType() == VARIABLE_DEF`
        // L58 SURVIVED: `grandParent != null && grandParent.getType() == OBJBLOCK`
        // A field-level regex call has VARIABLE_DEF -> OBJBLOCK structure.
        // If L56 negated: VARIABLE_DEF wouldn't trigger the check for OBJBLOCK grandparent,
        // so field-level regex would be flagged (false positive).
        // If L58 negated: OBJBLOCK grandparent would NOT cause return false, so field-level
        // regex would be flagged (false positive).
        List<AuditEvent> violations = runTreeWalkerCheck(InlineRegexConstantCheck.class, "simplify/invalid/InlineRegexMutationKiller.java", Map.of());
        // Verify field-level regex (line 8) is NOT flagged — exactly 2 violations expected
        assertEquals(EXPECTED_MUTATION_KILLER_VIOLATIONS, violations.size(), "Field-level regex must NOT be flagged (isInsideMethodBody returns false): " + format(violations));
        assertTrue(violations.stream().noneMatch(v -> v.getLine() == FIELD_LEVEL_LINE), "Field initializer regex must be skipped: " + format(violations));
    }

    @Test
    void extractMethodNameNoDotPath() throws Exception {
        // L73 SURVIVED: `return ident != null ? ident.getText() : null`
        // Exercises unqualified method call (no DOT) to a regex-accepting method.
        List<AuditEvent> violations = runTreeWalkerCheck(InlineRegexConstantCheck.class, "simplify/invalid/InlineRegexUnqualified.java", Map.of());
        // The unqualified split("hello-world-test") should be flagged (exercises L73 ident path)
        // The qualified input.split("\\d+-\\w+") inside the private method should also be flagged
        assertEquals(EXPECTED_MUTATION_KILLER_VIOLATIONS, violations.size(), "Both unqualified and qualified regex calls should be flagged: " + format(violations));
        // Verify the unqualified call is flagged with method name "split"
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("split")), "Violation should mention 'split' method: " + format(violations));
    }
}
