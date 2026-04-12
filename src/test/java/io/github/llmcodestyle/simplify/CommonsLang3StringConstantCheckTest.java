package io.github.llmcodestyle.simplify;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.github.llmcodestyle.utils.TestCheckSupportUtil.*;
import static org.junit.jupiter.api.Assertions.*;

class CommonsLang3StringConstantCheckTest {

    private static final Map<String, String> NO_PROPS = Map.of();
    private static final int EXPECTED_VIOLATIONS = 4;

    @Test
    void emptySpaceLfCrConstantsAreFlagged() throws Exception {
        List<AuditEvent> violations = run("simplify/invalid/CommonsLang3StringConstantInvalid.java");
        assertEquals(EXPECTED_VIOLATIONS, violations.size(), "Expected 4 violations: " + format(violations));
    }

    @Test
    void otherConstantsAndLocalsAreNotFlagged() throws Exception {
        assertTrue(run("simplify/valid/CommonsLang3StringConstantValid.java").isEmpty());
    }

    @Test
    void violationMessageContainsConstantInfo() throws Exception {
        List<AuditEvent> violations = run("simplify/invalid/CommonsLang3StringConstantInvalid.java");
        for (AuditEvent event : violations) {
            assertNotNull(event.getMessage(), "Message should not be null");
            assertFalse(event.getMessage().isEmpty(), "Message should not be empty");
        }
    }

    @Test
    void stringLiteralInitTextReturnsCorrectValue() throws Exception {
        // L89 SURVIVED: `return null` when assign == null
        // L93 NO_COVERAGE: `return null` when expr == null
        // Precise check that the message contains the field name and equivalent
        List<AuditEvent> violations = run("simplify/invalid/CommonsLang3MutKill.java");
        assertEquals(3, violations.size(),
            "BLANK, SEPARATOR, and NEWLINE should be flagged: " + format(violations));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("BLANK") && v.getMessage().contains("EMPTY")),
            "BLANK should suggest EMPTY: " + format(violations));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("SEPARATOR") && v.getMessage().contains("SPACE")),
            "SEPARATOR should suggest SPACE: " + format(violations));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("NEWLINE") && v.getMessage().contains("LF")),
            "NEWLINE should suggest LF: " + format(violations));
    }

    @Test
    void nonLiteralInitializerNotFlagged() throws Exception {
        // Exercises stringLiteralInitText returning null for non-string-literal initializers
        List<AuditEvent> violations = run("simplify/valid/CommonsLang3NoAssign.java");
        assertTrue(violations.isEmpty(),
            "Non-literal, concat, non-String, and mutable fields should not be flagged: " + format(violations));
    }

    private static List<AuditEvent> run(String resource) throws Exception {
        return runTreeWalkerCheck(CommonsLang3StringConstantCheck.class, resource, NO_PROPS);
    }
}
