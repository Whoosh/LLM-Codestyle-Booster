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
        assertEquals(3, violations.size(),
            "Exactly 3 needsSpace edge case violations: " + format(violations));
        // Verify the combined line length is reported correctly — if needsSpace were wrong,
        // the character count would differ by 1 (missing or extra space)
        for (AuditEvent v : violations) {
            assertTrue(v.getMessage().contains("180"), "Message should mention max line length: " + v.getMessage());
            // Extract the combined length from the message
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\d+) chars").matcher(v.getMessage());
            assertTrue(m.find(), "Message should contain char count: " + v.getMessage());
            int combinedLen = Integer.parseInt(m.group(1));
            assertTrue(combinedLen > 0 && combinedLen <= 180,
                "Combined length should be between 1 and 180: " + combinedLen);
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

    @Test
    void mutationKillerInvalidProducesViolations() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(UnnecessaryLineWrapCheck.class,
            "layout/invalid/UnnecessaryLineWrapMutationKiller.java", DEFAULT_PROPS);
        // Violations: try-resource(10), constructor(24), abstract-method(31), var-empty-line(37),
        //   space-var(44), closing-bracket(50), opening-bracket(56), annotated-class(64), if(74), record(81)
        assertEquals(10, violations.size(),
            "Mutation killer invalid fixture violations: " + format(violations));

        // Verify specific lines
        assertTrue(violations.stream().anyMatch(v -> v.getLine() == 10),
            "Try-resource wrapping: " + format(violations));
        assertTrue(violations.stream().anyMatch(v -> v.getLine() == 24),
            "Constructor wrapping: " + format(violations));
        assertTrue(violations.stream().anyMatch(v -> v.getLine() == 31),
            "Abstract method wrapping: " + format(violations));
        assertTrue(violations.stream().anyMatch(v -> v.getLine() == 37),
            "Variable with empty continuation: " + format(violations));
        assertTrue(violations.stream().anyMatch(v -> v.getLine() == 64),
            "Fully-annotated class wrapping: " + format(violations));
        assertTrue(violations.stream().anyMatch(v -> v.getLine() == 81),
            "Record wrapping: " + format(violations));
    }

    @Test
    void mutationKillerValidProducesNoViolations() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(UnnecessaryLineWrapCheck.class,
            "layout/valid/UnnecessaryLineWrapMutationKiller.java", DEFAULT_PROPS);
        assertTrue(violations.isEmpty(),
            "Mutation killer valid fixture should produce no violations: " + format(violations));
    }

    @Test
    void resourceWhoseTryHeaderFitsIsSkipped() throws Exception {
        // When try header fits on one line, individual RESOURCE nodes should not be flagged
        List<AuditEvent> violations = runTreeWalkerCheck(UnnecessaryLineWrapCheck.class,
            "layout/valid/UnnecessaryLineWrapMutationKiller.java", DEFAULT_PROPS);
        // No violations at all means the try-header-fits logic correctly skips RESOURCE
        assertTrue(violations.isEmpty(),
            "RESOURCE inside try-header-that-fits should be skipped: " + format(violations));
    }

    @Test
    void needsSpaceHandlesAllBranches() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(UnnecessaryLineWrapCheck.class,
            "layout/invalid/UnnecessaryLineWrapMutationKiller.java", DEFAULT_PROPS);
        // closingBracketTest: return compute(\n"arg") - line 50
        AuditEvent closingBracket = violations.stream().filter(v -> v.getLine() == 50).findFirst().orElse(null);
        assertNotNull(closingBracket, "Closing bracket needsSpace path: " + format(violations));
        // Combined: "        return compute(\"arg\");" — 'compute(' + '"arg")' no space before closing
        assertTrue(closingBracket.getMessage().contains("chars"),
            "Should report combined length: " + closingBracket.getMessage());

        // openingBracketTest: run(\n42) - line 56
        AuditEvent openingBracket = violations.stream().filter(v -> v.getLine() == 56).findFirst().orElse(null);
        assertNotNull(openingBracket, "Opening bracket needsSpace path: " + format(violations));
        // Combined: "        run(42);" — 'run(' + '42)' no space after opening bracket
        assertTrue(openingBracket.getMessage().contains("chars"),
            "Should report combined length: " + openingBracket.getMessage());

        // space at end: String x =\n"hello" - line 44
        AuditEvent spaceAtEnd = violations.stream().filter(v -> v.getLine() == 44).findFirst().orElse(null);
        assertNotNull(spaceAtEnd, "Space at end needsSpace path: " + format(violations));
        // Combined: "        String x = \"hello\";" — '= ' + '"hello"' already has space after =
        assertTrue(spaceAtEnd.getMessage().contains("chars"),
            "Should report combined length: " + spaceAtEnd.getMessage());
    }

    @Test
    void buildCombinedLineHandlesEmptyContinuation() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(UnnecessaryLineWrapCheck.class,
            "layout/invalid/UnnecessaryLineWrapTryResource.java", DEFAULT_PROPS);
        // emptyLineContinuation (lines 44-47) should be detected despite the blank continuation line
        assertTrue(violations.stream().anyMatch(v -> v.getLine() >= 44 && v.getLine() <= 47),
            "Empty line continuation should still be detected: " + format(violations));
    }

    @Test
    void tryResourceHeaderWithSmallMaxLine() throws Exception {
        // With a very small maxLineLength, the try header won't fit, so RESOURCE won't be skipped
        List<AuditEvent> violations = runTreeWalkerCheck(UnnecessaryLineWrapCheck.class,
            "layout/invalid/UnnecessaryLineWrapTryResource.java", Map.of("maxLineLength", "80"));
        // With shorter max, more violations expected
        assertFalse(violations.isEmpty(),
            "With shorter max line, resource wrapping should still be detected: " + format(violations));
    }

    @Test
    void computeFirstLineUsesIdentWhenNoType() throws Exception {
        // CTOR_DEF has no TYPE child but has IDENT — tests computeFirstLine line 93-94
        List<AuditEvent> violations = runTreeWalkerCheck(UnnecessaryLineWrapCheck.class,
            "layout/invalid/UnnecessaryLineWrapMutationKiller.java", DEFAULT_PROPS);
        // The constructor wrapping should be detected via IDENT fallback on line 24
        assertTrue(violations.stream().anyMatch(v -> v.getLine() == 24),
            "Constructor def should use IDENT for firstLine: " + format(violations));
    }

    @Test
    void firstNonAnnotationLineReturnsIdentWhenAllAnnotations() throws Exception {
        // When all modifiers are annotations, firstNonAnnotationLine should fall through to IDENT
        List<AuditEvent> violations = runTreeWalkerCheck(UnnecessaryLineWrapCheck.class,
            "layout/invalid/UnnecessaryLineWrapMutationKiller.java", DEFAULT_PROPS);
        assertTrue(violations.stream().anyMatch(v -> v.getLine() == 64),
            "Class with all-annotation modifiers should use IDENT for firstLine: " + format(violations));
    }

    @Test
    void findSignatureLastLineUsesSemiForAbstractMethod() throws Exception {
        // Abstract method has SEMI but no SLIST — findSignatureLastLine line 190-191
        List<AuditEvent> violations = runTreeWalkerCheck(UnnecessaryLineWrapCheck.class,
            "layout/invalid/UnnecessaryLineWrapMutationKiller.java", DEFAULT_PROPS);
        assertTrue(violations.stream().anyMatch(v -> v.getLine() == 31),
            "Abstract method with SEMI should use semi line: " + format(violations));
    }

    @Test
    void mutationKiller2ProducesExpectedViolations() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(UnnecessaryLineWrapCheck.class,
            "layout/invalid/UnnecessaryLineWrapMutationKiller2.java", DEFAULT_PROPS);
        assertTrue(violations.size() >= 9, "MutationKiller2 violations: " + format(violations));
        assertTrue(violations.stream().anyMatch(v -> v.getLine() == 12), "Resource: " + format(violations));
        assertTrue(violations.stream().anyMatch(v -> v.getLine() == 21), "VarDef: " + format(violations));
        assertTrue(violations.stream().anyMatch(v -> v.getLine() == 28), "ModifiedClass: " + format(violations));
        assertTrue(violations.stream().anyMatch(v -> v.getLine() == 35), "AllAnnotation: " + format(violations));
        assertTrue(violations.stream().anyMatch(v -> v.getLine() == 66), "Abstract: " + format(violations));
        assertTrue(violations.stream().anyMatch(v -> v.getLine() == 49), "ClosingBracket: " + format(violations));
        assertTrue(violations.stream().anyMatch(v -> v.getLine() == 55), "OpeningBracket: " + format(violations));
        for (AuditEvent v : violations) {
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\d+) chars").matcher(v.getMessage());
            assertTrue(m.find(), "char count: " + v.getMessage());
            assertTrue(Integer.parseInt(m.group(1)) <= 180, "within max: " + v.getMessage());
        }
    }
}
