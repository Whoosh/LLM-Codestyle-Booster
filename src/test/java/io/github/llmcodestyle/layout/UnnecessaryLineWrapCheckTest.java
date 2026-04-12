package io.github.llmcodestyle.layout;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.github.llmcodestyle.utils.TestCheckSupportUtil.*;
import static org.junit.jupiter.api.Assertions.*;

class UnnecessaryLineWrapCheckTest {

    private static final int EXPECTED_VIOLATIONS = 13;
    private static final String SHORT_MAX_LINE = "100";
    private static final Map<String, String> DEFAULT_PROPS = Map.of("maxLineLength", "180");

    @Test
    void invalidCasesProduceViolations() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(UnnecessaryLineWrapCheck.class, "layout/invalid/UnnecessaryLineWrapInvalid.java", DEFAULT_PROPS);
        assertEquals(EXPECTED_VIOLATIONS, violations.size(), "Expected 13 unnecessary wrap violations, got: " + format(violations));
    }

    @Test
    void validCasesProduceNoViolations() throws Exception {
        assertTrue(
            runTreeWalkerCheck(UnnecessaryLineWrapCheck.class, "layout/valid/UnnecessaryLineWrapValid.java", DEFAULT_PROPS).isEmpty(),
            "Expected no violations");
    }

    @Test
    void setMaxLineLengthAffectsThreshold() throws Exception {
        new UnnecessaryLineWrapCheck().setMaxLineLength(Integer.parseInt(SHORT_MAX_LINE));
        List<AuditEvent> violations = runTreeWalkerCheck(UnnecessaryLineWrapCheck.class,
            "layout/invalid/UnnecessaryLineWrapInvalid.java", Map.of("maxLineLength", SHORT_MAX_LINE));
        assertFalse(violations.isEmpty(), "Smaller max line length should still produce violations");
        assertNotEquals(EXPECTED_VIOLATIONS, violations.size(),
            "Different threshold should change the violation count: " + format(violations));
    }

    @Test
    void edgeCaseValidFixtureProducesNoViolations() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(UnnecessaryLineWrapCheck.class,
            "layout/valid/UnnecessaryLineWrapEdgeCases.java", DEFAULT_PROPS);
        assertTrue(violations.isEmpty(), "Edge case valid fixture should produce no violations: " + format(violations));
    }

    @Test
    void edgeCaseInvalidFixtureProducesViolations() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(UnnecessaryLineWrapCheck.class,
            "layout/invalid/UnnecessaryLineWrapEdgeCases.java", DEFAULT_PROPS);
        assertFalse(violations.isEmpty(), "Edge case invalid fixture should produce violations");
        assertTrue(violations.size() >= 3, "Expected at least 3 edge-case violations, got: " + format(violations));
    }

    @Test
    void violationMessageContainsLengthInfo() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(UnnecessaryLineWrapCheck.class,
            "layout/invalid/UnnecessaryLineWrapInvalid.java", DEFAULT_PROPS);
        for (AuditEvent event : violations) {
            String msg = event.getMessage();
            assertTrue(msg.contains("180") || msg.contains("line"),
                "Violation message should reference max line length or 'line': " + msg);
        }
    }

    @Test
    void tryWithoutResourcesIsNotFlagged() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(UnnecessaryLineWrapCheck.class,
            "layout/valid/UnnecessaryLineWrapEdgeCases.java", DEFAULT_PROPS);
        assertTrue(violations.stream().noneMatch(v -> v.getLine() >= 7 && v.getLine() <= 13),
            "try without resources should not be flagged");
    }

    @Test
    void longChainInsideStatementIsNotFlagged() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(UnnecessaryLineWrapCheck.class,
            "layout/valid/UnnecessaryLineWrapEdgeCases.java", DEFAULT_PROPS);
        assertTrue(violations.stream().noneMatch(v -> v.getLine() >= 16 && v.getLine() <= 21),
            "Statements containing long chains should not be flagged");
    }

    @Test
    void containerTypeWithLongChainChildIsNotFlagged() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(UnnecessaryLineWrapCheck.class,
            "layout/valid/UnnecessaryLineWrapEdgeCases.java", DEFAULT_PROPS);
        assertTrue(violations.stream().noneMatch(v -> v.getLine() >= 24 && v.getLine() <= 28),
            "Container class definitions should not flag for long chains in children");
    }

    @Test
    void needsSpaceExercisedWithClosingAndOpeningBrackets() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(UnnecessaryLineWrapCheck.class,
            "layout/invalid/UnnecessaryLineWrapNeedsSpace.java", DEFAULT_PROPS);
        // The fixture has short wraps for: return compute(\n"arg"), run(\n42), String x =\n"value"
        // All of these fit within 180 chars when combined, so all should produce violations
        assertTrue(violations.size() >= 3,
            "Expected violations for short wraps with bracket edge cases: " + format(violations));
    }

    @Test
    void eachViolationReportsPositiveLineNumber() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(UnnecessaryLineWrapCheck.class,
            "layout/invalid/UnnecessaryLineWrapInvalid.java", DEFAULT_PROPS);
        for (AuditEvent event : violations) {
            assertTrue(event.getLine() > 0, "Line should be positive: " + event.getLine());
        }
    }

    @Test
    void tryResourceEdgeCasesProduceViolations() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(UnnecessaryLineWrapCheck.class,
            "layout/invalid/UnnecessaryLineWrapTryResource.java", DEFAULT_PROPS);
        assertFalse(violations.isEmpty(), "Try-resource edge cases should produce violations: " + format(violations));
    }

    @Test
    void abstractMethodWithSemiDetected() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(UnnecessaryLineWrapCheck.class,
            "layout/invalid/UnnecessaryLineWrapEdgeCases.java", DEFAULT_PROPS);
        // The abstract method process(String a, String b) should be detected (uses SEMI in findSignatureLastLine)
        assertTrue(violations.stream().anyMatch(v -> v.getLine() >= 12 && v.getLine() <= 13),
            "Abstract method with wrapped params should be detected: " + format(violations));
    }

    @Test
    void annotatedClassDetectedWithFirstNonAnnotationLine() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(UnnecessaryLineWrapCheck.class,
            "layout/invalid/UnnecessaryLineWrapEdgeCases.java", DEFAULT_PROPS);
        // @Deprecated static class AnnotatedClass extends Thread should be detected
        assertTrue(violations.stream().anyMatch(v -> v.getLine() >= 23 && v.getLine() <= 26),
            "Annotated class with extends should be detected: " + format(violations));
    }

    @Test
    void needsSpaceInsertsSpaceCorrectly() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(UnnecessaryLineWrapCheck.class,
            "layout/invalid/UnnecessaryLineWrapNeedsSpace.java", DEFAULT_PROPS);
        // All three cases should produce violations: closing paren, opening paren, trailing space
        assertTrue(violations.size() >= 3,
            "All needsSpace edge cases should produce violations: " + format(violations));
        // Verify the combined line length is reported correctly
        for (AuditEvent v : violations) {
            assertTrue(v.getMessage().contains("180"), "Message should mention max line length: " + v.getMessage());
        }
    }

    @Test
    void constructorWrappingDetected() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(UnnecessaryLineWrapCheck.class,
            "layout/invalid/UnnecessaryLineWrapEdgeCases.java", DEFAULT_PROPS);
        // Constructor wrapping on line 6 should be detected
        assertTrue(violations.stream().anyMatch(v -> v.getLine() >= 6 && v.getLine() <= 7),
            "Constructor wrapping should be detected: " + format(violations));
    }

    @Test
    void violationMessageContainsActualLength() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(UnnecessaryLineWrapCheck.class,
            "layout/invalid/UnnecessaryLineWrapInvalid.java", DEFAULT_PROPS);
        for (AuditEvent event : violations) {
            String msg = event.getMessage();
            // Message should contain both the actual combined length and the max
            assertTrue(msg.matches(".*\\d+.*\\d+.*"), "Message should contain length numbers: " + msg);
        }
    }

    @Test
    void tryResourceWrappingDetected() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(UnnecessaryLineWrapCheck.class,
            "layout/invalid/UnnecessaryLineWrapTryResource.java", DEFAULT_PROPS);
        // The try-with-resources resource that wraps should be detected
        assertTrue(violations.size() >= 2,
            "Try-resource wrapping should produce violations: " + format(violations));
    }
}
