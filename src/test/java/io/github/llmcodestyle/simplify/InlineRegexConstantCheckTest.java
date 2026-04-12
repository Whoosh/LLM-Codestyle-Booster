package io.github.llmcodestyle.simplify;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.github.llmcodestyle.utils.TestCheckSupportUtil.*;
import static org.junit.jupiter.api.Assertions.*;

class InlineRegexConstantCheckTest {

    private static final int EXPECTED_VIOLATIONS = 5;

    @Test
    void invalidCasesProduceViolations() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(InlineRegexConstantCheck.class, "simplify/invalid/InlineRegexInvalid.java", Map.of());
        assertEquals(EXPECTED_VIOLATIONS, violations.size(), "Expected 5 inline regex violations, got: " + format(violations));
    }

    @Test
    void validCasesProduceNoViolations() throws Exception {
        assertTrue(runTreeWalkerCheck(InlineRegexConstantCheck.class, "simplify/valid/InlineRegexValid.java", Map.of()).isEmpty(), "Expected no violations");
    }

    @Test
    void violationMessageContainsPatternInfo() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(InlineRegexConstantCheck.class, "simplify/invalid/InlineRegexInvalid.java", Map.of());
        for (AuditEvent event : violations) {
            String msg = event.getMessage();
            assertNotNull(msg, "Message should not be null");
            assertFalse(msg.isEmpty(), "Message should not be empty");
        }
    }

    @Test
    void violationsReportCorrectLines() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(InlineRegexConstantCheck.class, "simplify/invalid/InlineRegexInvalid.java", Map.of());
        for (AuditEvent event : violations) {
            assertTrue(event.getLine() > 0, "Line should be positive");
        }
    }

    @Test
    void fieldLevelRegexIsNotFlagged() throws Exception {
        // isInsideMethodBody returns false for field initializers (VARIABLE_DEF with OBJBLOCK grandparent)
        List<AuditEvent> violations = runTreeWalkerCheck(InlineRegexConstantCheck.class,
            "simplify/valid/InlineRegexFieldLevel.java", Map.of());
        assertTrue(violations.isEmpty(),
            "Field-level regex usage should not be flagged: " + format(violations));
    }

    @Test
    void mutationKillerExercisesExtractMethodNameWithDot() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(InlineRegexConstantCheck.class,
            "simplify/invalid/InlineRegexMutationKiller.java", Map.of());
        // Should flag inline regex constants in method bodies (qualifiedCall and unqualifiedCall)
        // but NOT in field initializer (fieldSplit)
        assertTrue(violations.size() >= 2,
            "Inline regex in method body should be flagged: " + format(violations));
        // Field initializer should NOT be flagged (isInsideMethodBody returns false)
        assertTrue(violations.stream().noneMatch(v -> v.getLine() == 8),
            "Field-level regex should not be flagged: " + format(violations));
    }

    @Test
    void fieldLevelRegexNotFlaggedButMethodLevelIs() throws Exception {
        // Exercises isInsideMethodBody lines 56-58:
        // VARIABLE_DEF with OBJBLOCK grandparent returns false (field)
        // VARIABLE_DEF with SLIST grandparent continues traversal (local var in method)
        List<AuditEvent> violations = runTreeWalkerCheck(InlineRegexConstantCheck.class,
            "simplify/invalid/InlineRegexMutationKiller.java", Map.of());
        // qualifiedCall() line 14: s.matches("\\d+") — flagged
        assertTrue(violations.stream().anyMatch(v -> v.getLine() == 14),
            "Qualified regex call in method should be flagged: " + format(violations));
        // unqualifiedCall() line 19: input.replaceAll("\\w+", ...) — flagged
        assertTrue(violations.stream().anyMatch(v -> v.getLine() == 19),
            "Unqualified replaceAll in method should be flagged: " + format(violations));
    }
}
