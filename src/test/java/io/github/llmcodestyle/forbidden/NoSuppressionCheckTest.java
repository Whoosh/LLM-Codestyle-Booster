package io.github.llmcodestyle.forbidden;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.github.llmcodestyle.utils.TestCheckSupportUtil.*;
import static org.junit.jupiter.api.Assertions.*;

class NoSuppressionCheckTest {

    private static final int EXPECTED_VIOLATIONS = 4;
    private static final Map<String, String> NO_PROPS = Map.of();

    @Test
    void invalidCasesProduceViolations() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(NoSuppressionCheck.class, "forbidden/invalid/NoSuppressionInvalid.java", NO_PROPS);
        assertEquals(EXPECTED_VIOLATIONS, violations.size(), "Expected 4 suppression violations (2 comments + 2 annotations), got: " + format(violations));
    }

    @Test
    void validCasesProduceNoViolations() throws Exception {
        assertTrue(runTreeWalkerCheck(NoSuppressionCheck.class, "forbidden/valid/NoSuppressionValid.java", NO_PROPS).isEmpty(), "Expected no violations");
    }

    @Test
    void suppressionKeywordsInStringLiteralsAreNotFlagged() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(NoSuppressionCheck.class, "forbidden/valid/NoSuppressionEdgeCases.java", NO_PROPS);
        assertTrue(violations.isEmpty(), "Suppression keywords inside string literals should not be flagged: " + format(violations));
    }

    @Test
    void edgeCaseInvalidFixtureProducesViolations() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(NoSuppressionCheck.class, "forbidden/invalid/NoSuppressionEdgeCases.java", NO_PROPS);
        // 3 comment suppressions (NOPMD, CHECKSTYLE:OFF, SUPPRESSFBWARNINGS) + 2 annotations
        assertEquals(5, violations.size(), "Expected 5 violations in edge case fixture: " + format(violations));
    }

    @Test
    void commentViolationsReportCorrectLineNumbers() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(NoSuppressionCheck.class, "forbidden/invalid/NoSuppressionInvalid.java", NO_PROPS);
        // Lines 3 and 4 have comment suppressions, lines 7 and 11 have annotation suppressions
        assertTrue(violations.stream().anyMatch(v -> v.getLine() == 3), "Should detect NOPMD on line 3: " + format(violations));
        assertTrue(violations.stream().anyMatch(v -> v.getLine() == 4), "Should detect CHECKSTYLE:OFF on line 4: " + format(violations));
    }

    @Test
    void annotationViolationMessageContainsAnnotationName() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(NoSuppressionCheck.class, "forbidden/invalid/NoSuppressionInvalid.java", NO_PROPS);
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("SuppressWarnings")),
            "Should mention SuppressWarnings in message: " + format(violations));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("SuppressFBWarnings")),
            "Should mention SuppressFBWarnings in message: " + format(violations));
    }

    @Test
    void escapedQuoteInsideStringDoesNotBreakParsing() throws Exception {
        // The edge case valid fixture has escaped quotes, backslashes, and char literals
        List<AuditEvent> violations = runTreeWalkerCheck(NoSuppressionCheck.class, "forbidden/valid/NoSuppressionEdgeCases.java", NO_PROPS);
        assertTrue(violations.isEmpty(), "Escaped characters should not confuse the comment parser: " + format(violations));
    }

    @Test
    void suppressionCommentsAfterStringEscapesAreDetected() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(NoSuppressionCheck.class, "forbidden/invalid/NoSuppressionStringEscape.java", NO_PROPS);
        // 3 comment suppressions: NOPMD, CHECKSTYLE:OFF, SUPPRESSFBWARNINGS
        assertEquals(3, violations.size(),
            "Suppression comments after escaped strings should be detected: " + format(violations));
    }

    @Test
    void charAndStringEdgeCasesDetected() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(NoSuppressionCheck.class, "forbidden/invalid/NoSuppressionCharAndStringEdge.java", NO_PROPS);
        // 5 comment suppressions after various char/string edge cases
        assertEquals(5, violations.size(),
            "All suppression comments after char/string edge cases should be detected: " + format(violations));
        // Verify specific lines are detected
        assertTrue(violations.stream().anyMatch(v -> v.getLine() == 6), "char backslash: " + format(violations));
        assertTrue(violations.stream().anyMatch(v -> v.getLine() == 9), "string dq: " + format(violations));
        assertTrue(violations.stream().anyMatch(v -> v.getLine() == 12), "char sq: " + format(violations));
        assertTrue(violations.stream().anyMatch(v -> v.getLine() == 15), "mixed: " + format(violations));
        assertTrue(violations.stream().anyMatch(v -> v.getLine() == 18), "multi: " + format(violations));
    }

    @Test
    void suppressionKeywordsInsideStringsAndCharsNotFlagged() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(NoSuppressionCheck.class, "forbidden/valid/NoSuppressionCharStringValid.java", NO_PROPS);
        assertTrue(violations.isEmpty(),
            "Suppression keywords inside strings/chars should not be flagged: " + format(violations));
    }

    @Test
    void findCommentStartCharQuoteToggling() throws Exception {
        // L88-89 SURVIVED: `c == '\'' -> inChar = !inChar`
        // L92 SURVIVED: `!inString && !inChar && c == '/' && line.charAt(i+1) == '/'`
        // Tests single-quote escapes inside char literals, then suppression comment after.
        // If inChar toggling is broken, the parser would think we're inside a char literal
        // when we're not, and miss the comment.
        List<AuditEvent> violations = runTreeWalkerCheck(NoSuppressionCheck.class,
            "forbidden/invalid/NoSuppressionFindCommentEdge.java", NO_PROPS);
        // 3 comment suppressions on lines 6, 8, 10
        long commentViolations = violations.stream()
            .filter(v -> v.getMessage().contains("comment")).count();
        assertEquals(3, commentViolations,
            "All 3 suppression comments after char/string edge cases should be detected: " + format(violations));
        // Verify each specific line
        assertTrue(violations.stream().anyMatch(v -> v.getLine() == 6),
            "NOPMD after char sq-escape: " + format(violations));
        assertTrue(violations.stream().anyMatch(v -> v.getLine() == 8),
            "CHECKSTYLE:OFF after string dq-escape: " + format(violations));
        assertTrue(violations.stream().anyMatch(v -> v.getLine() == 10),
            "SUPPRESSFBWARNINGS after char backslash: " + format(violations));
    }
}
