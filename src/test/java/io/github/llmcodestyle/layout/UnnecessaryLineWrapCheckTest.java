package io.github.llmcodestyle.layout;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static io.github.llmcodestyle.utils.TestCheckSupportUtil.*;
import static org.junit.jupiter.api.Assertions.*;

class UnnecessaryLineWrapCheckTest {

    private static final int EXPECTED_VIOLATIONS = 13;
    private static final String SHORT_MAX_LINE = "100";
    private static final Map<String, String> DEFAULT_PROPS = Map.of("maxLineLength", "180");
    private static final int MIN_EDGE_CASE_VIOLATIONS = 3;
    private static final int MAX_LINE_LENGTH = 180;
    private static final int TRY_START = 7;
    private static final int TRY_END = 13;
    private static final int CHAIN_START = 16;
    private static final int CHAIN_END = 21;
    private static final int CONTAINER_START = 24;
    private static final int CONTAINER_END = 28;
    private static final int ABSTRACT_METHOD_START = 12;
    private static final int ABSTRACT_METHOD_END = 13;
    private static final int ANNOTATED_CLASS_START = 23;
    private static final int ANNOTATED_CLASS_END = 26;
    private static final int MK_LINE_TRY_RESOURCE = 10;
    private static final int MK_LINE_CTOR = 24;
    private static final int MK_LINE_ABSTRACT = 31;
    private static final int MK_LINE_VAR_EMPTY = 37;
    private static final int MK_LINE_ANNOTATED_CLASS = 64;
    private static final int MK_LINE_RECORD = 81;
    private static final int MK_LINE_SPACE_VAR = 44;
    private static final int MK_LINE_CLOSING_BRACKET = 50;
    private static final int MK_LINE_OPENING_BRACKET = 56;
    private static final int MK_EXPECTED_VIOLATIONS = 10;
    private static final int MK2_MIN_VIOLATIONS = 9;
    private static final int MK2_LINE_RESOURCE = 12;
    private static final int MK2_LINE_VARDEF = 21;
    private static final int MK2_LINE_MODIFIED_CLASS = 28;
    private static final int MK2_LINE_ALL_ANNOTATION = 35;
    private static final int MK2_LINE_ABSTRACT = 66;
    private static final int MK2_LINE_CLOSING_BRACKET = 49;
    private static final int MK2_LINE_OPENING_BRACKET = 55;
    private static final int MK2_LINE_SPACE_AT_END = 44;
    private static final int MK2_EXPECTED_CLOSING_LEN = 30;
    private static final int MK2_EXPECTED_OPENING_LEN = 16;
    private static final int MK2_EXPECTED_SPACE_LEN = 28;
    private static final int MK3_LINE_RESOURCE = 13;
    private static final int MK3_LINE_CTOR = 23;
    private static final int MK3_LINE_ABSTRACT = 50;
    private static final int MK3_LINE_RESULT = 62;
    private static final int MK3_EXPECTED_RESULT_LEN = 40;
    private static final int EMPTY_CONT_START = 44;
    private static final int EMPTY_CONT_END = 47;
    private static final Pattern CHAR_COUNT_PATTERN = Pattern.compile("(\\d+) chars");

    @Test
    void invalidCasesProduceViolations() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(UnnecessaryLineWrapCheck.class, "layout/invalid/UnnecessaryLineWrapInvalid.java", DEFAULT_PROPS);
        assertEquals(EXPECTED_VIOLATIONS, violations.size(), "Expected 13 unnecessary wrap violations, got: " + format(violations));
    }

    @Test
    void validCasesProduceNoViolations() throws Exception {
        assertTrue(runTreeWalkerCheck(UnnecessaryLineWrapCheck.class, "layout/valid/UnnecessaryLineWrapValid.java", DEFAULT_PROPS).isEmpty(), "Expected no violations");
    }

    @Test
    void setMaxLineLengthAffectsThreshold() throws Exception {
        new UnnecessaryLineWrapCheck().setMaxLineLength(Integer.parseInt(SHORT_MAX_LINE));
        List<AuditEvent> violations = runTreeWalkerCheck(UnnecessaryLineWrapCheck.class, "layout/invalid/UnnecessaryLineWrapInvalid.java", Map.of("maxLineLength", SHORT_MAX_LINE));
        assertFalse(violations.isEmpty(), "Smaller max line length should still produce violations");
        assertNotEquals(EXPECTED_VIOLATIONS, violations.size(), "Different threshold should change the violation count: " + format(violations));
    }

    @Test
    void edgeCaseValidFixtureProducesNoViolations() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(UnnecessaryLineWrapCheck.class, "layout/valid/UnnecessaryLineWrapEdgeCases.java", DEFAULT_PROPS);
        assertTrue(violations.isEmpty(), "Edge case valid fixture should produce no violations: " + format(violations));
    }

    @Test
    void edgeCaseInvalidFixtureProducesViolations() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(UnnecessaryLineWrapCheck.class, "layout/invalid/UnnecessaryLineWrapEdgeCases.java", DEFAULT_PROPS);
        assertFalse(violations.isEmpty(), "Edge case invalid fixture should produce violations");
        assertTrue(violations.size() >= MIN_EDGE_CASE_VIOLATIONS, "Expected at least 3 edge-case violations, got: " + format(violations));
    }

    @Test
    void violationMessageContainsLengthInfo() throws Exception {
        for (AuditEvent event : runTreeWalkerCheck(UnnecessaryLineWrapCheck.class, "layout/invalid/UnnecessaryLineWrapInvalid.java", DEFAULT_PROPS)) {
            String msg = event.getMessage();
            assertTrue(msg.contains("180") || msg.contains("line"), "Violation message should reference max line length or 'line': " + msg);
        }
    }

    @Test
    void tryWithoutResourcesIsNotFlagged() throws Exception {
        assertTrue(runTreeWalkerCheck(UnnecessaryLineWrapCheck.class, "layout/valid/UnnecessaryLineWrapEdgeCases.java", DEFAULT_PROPS)
            .stream().noneMatch(v -> v.getLine() >= TRY_START && v.getLine() <= TRY_END), "try without resources should not be flagged");
    }

    @Test
    void longChainInsideStatementIsNotFlagged() throws Exception {
        assertTrue(runTreeWalkerCheck(UnnecessaryLineWrapCheck.class, "layout/valid/UnnecessaryLineWrapEdgeCases.java", DEFAULT_PROPS)
            .stream().noneMatch(v -> v.getLine() >= CHAIN_START && v.getLine() <= CHAIN_END), "Statements containing long chains should not be flagged");
    }

    @Test
    void containerTypeWithLongChainChildIsNotFlagged() throws Exception {
        assertTrue(runTreeWalkerCheck(UnnecessaryLineWrapCheck.class, "layout/valid/UnnecessaryLineWrapEdgeCases.java", DEFAULT_PROPS)
            .stream().noneMatch(v -> v.getLine() >= CONTAINER_START && v.getLine() <= CONTAINER_END),
            "Container class definitions should not flag for long chains in children");
    }

    @Test
    void needsSpaceExercisedWithClosingAndOpeningBrackets() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(UnnecessaryLineWrapCheck.class, "layout/invalid/UnnecessaryLineWrapNeedsSpace.java", DEFAULT_PROPS);
        // The fixture has short wraps for: return compute(\n"arg"), run(\n42), String x =\n"value"
        // All of these fit within 180 chars when combined, so all should produce violations
        assertTrue(violations.size() >= MIN_EDGE_CASE_VIOLATIONS, "Expected violations for short wraps with bracket edge cases: " + format(violations));
    }

    @Test
    void eachViolationReportsPositiveLineNumber() throws Exception {
        for (AuditEvent event : runTreeWalkerCheck(UnnecessaryLineWrapCheck.class, "layout/invalid/UnnecessaryLineWrapInvalid.java", DEFAULT_PROPS)) {
            assertTrue(event.getLine() > 0, "Line should be positive: " + event.getLine());
        }
    }

    @Test
    void tryResourceEdgeCasesProduceViolations() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(UnnecessaryLineWrapCheck.class, "layout/invalid/UnnecessaryLineWrapTryResource.java", DEFAULT_PROPS);
        assertFalse(violations.isEmpty(), "Try-resource edge cases should produce violations: " + format(violations));
    }

    @Test
    void abstractMethodWithSemiDetected() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(UnnecessaryLineWrapCheck.class, "layout/invalid/UnnecessaryLineWrapEdgeCases.java", DEFAULT_PROPS);
        // The abstract method process(String a, String b) should be detected (uses SEMI in findSignatureLastLine)
        assertTrue(violations.stream().anyMatch(v -> v.getLine() >= ABSTRACT_METHOD_START && v.getLine() <= ABSTRACT_METHOD_END),
            "Abstract method with wrapped params should be detected: " + format(violations));
    }

    @Test
    void annotatedClassDetectedWithFirstNonAnnotationLine() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(UnnecessaryLineWrapCheck.class, "layout/invalid/UnnecessaryLineWrapEdgeCases.java", DEFAULT_PROPS);
        // @Deprecated static class AnnotatedClass extends Thread should be detected
        assertTrue(violations.stream().anyMatch(v -> v.getLine() >= ANNOTATED_CLASS_START && v.getLine() <= ANNOTATED_CLASS_END),
            "Annotated class with extends should be detected: " + format(violations));
    }

    @Test
    void needsSpaceInsertsSpaceCorrectly() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(UnnecessaryLineWrapCheck.class, "layout/invalid/UnnecessaryLineWrapNeedsSpace.java", DEFAULT_PROPS);
        assertEquals(MIN_EDGE_CASE_VIOLATIONS, violations.size(), "Exactly 3 needsSpace edge case violations: " + format(violations));
        for (AuditEvent v : violations) {
            java.util.regex.Matcher m = CHAR_COUNT_PATTERN.matcher(v.getMessage());
            assertTrue(m.find(), "char count: " + v.getMessage());
            int len = Integer.parseInt(m.group(1));
            assertTrue(len > 0 && len <= MAX_LINE_LENGTH, "Combined length check: " + v.getMessage());
        }
    }

    @Test
    void tryResourceWrappingDetected() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(UnnecessaryLineWrapCheck.class, "layout/invalid/UnnecessaryLineWrapTryResource.java", DEFAULT_PROPS);
        // The try-with-resources resource that wraps should be detected
        assertTrue(violations.size() >= 2, "Try-resource wrapping should produce violations: " + format(violations));
    }

    @Test
    void mutationKillerInvalidProducesViolations() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(UnnecessaryLineWrapCheck.class, "layout/invalid/UnnecessaryLineWrapMutationKiller.java", DEFAULT_PROPS);
        // Violations: try-resource(10), constructor(24), abstract-method(31), var-empty-line(37),
        //   space-var(44), closing-bracket(50), opening-bracket(56), annotated-class(64), if(74), record(81)
        assertEquals(MK_EXPECTED_VIOLATIONS, violations.size(), "Mutation killer invalid fixture violations: " + format(violations));

        // Verify specific lines
        assertTrue(violations.stream().anyMatch(v -> v.getLine() == MK_LINE_TRY_RESOURCE), "Try-resource wrapping: " + format(violations));
        assertTrue(violations.stream().anyMatch(v -> v.getLine() == MK_LINE_CTOR), "Constructor wrapping: " + format(violations));
        assertTrue(violations.stream().anyMatch(v -> v.getLine() == MK_LINE_ABSTRACT), "Abstract method wrapping: " + format(violations));
        assertTrue(violations.stream().anyMatch(v -> v.getLine() == MK_LINE_VAR_EMPTY), "Variable with empty continuation: " + format(violations));
        assertTrue(violations.stream().anyMatch(v -> v.getLine() == MK_LINE_ANNOTATED_CLASS), "Fully-annotated class wrapping: " + format(violations));
        assertTrue(violations.stream().anyMatch(v -> v.getLine() == MK_LINE_RECORD), "Record wrapping: " + format(violations));
    }

    @Test
    void mutationKillerValidProducesNoViolations() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(UnnecessaryLineWrapCheck.class, "layout/valid/UnnecessaryLineWrapMutationKiller.java", DEFAULT_PROPS);
        assertTrue(violations.isEmpty(), "Mutation killer valid fixture should produce no violations: " + format(violations));
    }

    @Test
    void resourceWhoseTryHeaderFitsIsSkipped() throws Exception {
        // When try header fits on one line, individual RESOURCE nodes should not be flagged
        List<AuditEvent> violations = runTreeWalkerCheck(UnnecessaryLineWrapCheck.class, "layout/valid/UnnecessaryLineWrapMutationKiller.java", DEFAULT_PROPS);
        // No violations at all means the try-header-fits logic correctly skips RESOURCE
        assertTrue(violations.isEmpty(), "RESOURCE inside try-header-that-fits should be skipped: " + format(violations));
    }

    @Test
    void needsSpaceHandlesAllBranches() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(UnnecessaryLineWrapCheck.class, "layout/invalid/UnnecessaryLineWrapMutationKiller.java", DEFAULT_PROPS);
        // closingBracketTest: return compute(\n"arg") - line 50
        AuditEvent closingBracket = violations
            .stream()
            .filter(v -> v.getLine() == MK_LINE_CLOSING_BRACKET)
            .findFirst()
            .orElse(null);
        assertNotNull(closingBracket, "Closing bracket needsSpace path: " + format(violations));
        // Combined: "        return compute(\"arg\");" — 'compute(' + '"arg")' no space before closing
        assertTrue(closingBracket.getMessage().contains("chars"), "Should report combined length: " + closingBracket.getMessage());

        // openingBracketTest: run(\n42) - line 56
        AuditEvent openingBracket = violations
            .stream()
            .filter(v -> v.getLine() == MK_LINE_OPENING_BRACKET)
            .findFirst()
            .orElse(null);
        assertNotNull(openingBracket, "Opening bracket needsSpace path: " + format(violations));
        // Combined: "        run(42);" — 'run(' + '42)' no space after opening bracket
        assertTrue(openingBracket.getMessage().contains("chars"), "Should report combined length: " + openingBracket.getMessage());

        // space at end: String x =\n"hello" - line 44
        AuditEvent spaceAtEnd = violations
            .stream()
            .filter(v -> v.getLine() == MK_LINE_SPACE_VAR)
            .findFirst()
            .orElse(null);
        assertNotNull(spaceAtEnd, "Space at end needsSpace path: " + format(violations));
        // Combined: "        String x = \"hello\";" — '= ' + '"hello"' already has space after =
        assertTrue(spaceAtEnd.getMessage().contains("chars"), "Should report combined length: " + spaceAtEnd.getMessage());
    }

    @Test
    void buildCombinedLineHandlesEmptyContinuation() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(UnnecessaryLineWrapCheck.class, "layout/invalid/UnnecessaryLineWrapTryResource.java", DEFAULT_PROPS);
        // emptyLineContinuation (lines 44-47) should be detected despite the blank continuation line
        assertTrue(violations.stream().anyMatch(v -> v.getLine() >= EMPTY_CONT_START && v.getLine() <= EMPTY_CONT_END),
            "Empty line continuation should still be detected: " + format(violations));
    }

    @Test
    void tryResourceHeaderWithSmallMaxLine() throws Exception {
        // With a very small maxLineLength, the try header won't fit, so RESOURCE won't be skipped
        List<AuditEvent> violations = runTreeWalkerCheck(UnnecessaryLineWrapCheck.class, "layout/invalid/UnnecessaryLineWrapTryResource.java", Map.of("maxLineLength", "80"));
        // With shorter max, more violations expected
        assertFalse(violations.isEmpty(), "With shorter max line, resource wrapping should still be detected: " + format(violations));
    }

    @Test
    void computeFirstLineUsesIdentWhenNoType() throws Exception {
        // CTOR_DEF has no TYPE child but has IDENT — tests computeFirstLine line 93-94
        List<AuditEvent> violations = runTreeWalkerCheck(UnnecessaryLineWrapCheck.class, "layout/invalid/UnnecessaryLineWrapMutationKiller.java", DEFAULT_PROPS);
        // The constructor wrapping should be detected via IDENT fallback on line 24
        assertTrue(violations.stream().anyMatch(v -> v.getLine() == MK_LINE_CTOR), "Constructor def should use IDENT for firstLine: " + format(violations));
    }

    @Test
    void firstNonAnnotationLineReturnsIdentWhenAllAnnotations() throws Exception {
        // When all modifiers are annotations, firstNonAnnotationLine should fall through to IDENT
        List<AuditEvent> violations = runTreeWalkerCheck(UnnecessaryLineWrapCheck.class, "layout/invalid/UnnecessaryLineWrapMutationKiller.java", DEFAULT_PROPS);
        assertTrue(violations.stream().anyMatch(v -> v.getLine() == MK_LINE_ANNOTATED_CLASS),
            "Class with all-annotation modifiers should use IDENT for firstLine: " + format(violations));
    }

    @Test
    void findSignatureLastLineUsesSemiForAbstractMethod() throws Exception {
        // Abstract method has SEMI but no SLIST — findSignatureLastLine line 190-191
        List<AuditEvent> violations = runTreeWalkerCheck(UnnecessaryLineWrapCheck.class, "layout/invalid/UnnecessaryLineWrapMutationKiller.java", DEFAULT_PROPS);
        assertTrue(violations.stream().anyMatch(v -> v.getLine() == MK_LINE_ABSTRACT), "Abstract method with SEMI should use semi line: " + format(violations));
    }

    @Test
    void mutationKiller2ProducesExpectedViolations() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(UnnecessaryLineWrapCheck.class, "layout/invalid/UnnecessaryLineWrapMutationKiller2.java", DEFAULT_PROPS);
        assertTrue(violations.size() >= MK2_MIN_VIOLATIONS, "MutationKiller2 violations: " + format(violations));
        assertTrue(violations.stream().anyMatch(v -> v.getLine() == MK2_LINE_RESOURCE), "Resource: " + format(violations));
        assertTrue(violations.stream().anyMatch(v -> v.getLine() == MK2_LINE_VARDEF), "VarDef: " + format(violations));
        assertTrue(violations.stream().anyMatch(v -> v.getLine() == MK2_LINE_MODIFIED_CLASS), "ModifiedClass: " + format(violations));
        assertTrue(violations.stream().anyMatch(v -> v.getLine() == MK2_LINE_ALL_ANNOTATION), "AllAnnotation: " + format(violations));
        assertTrue(violations.stream().anyMatch(v -> v.getLine() == MK2_LINE_ABSTRACT), "Abstract: " + format(violations));
        assertTrue(violations.stream().anyMatch(v -> v.getLine() == MK2_LINE_CLOSING_BRACKET), "ClosingBracket: " + format(violations));
        assertTrue(violations.stream().anyMatch(v -> v.getLine() == MK2_LINE_OPENING_BRACKET), "OpeningBracket: " + format(violations));
        for (AuditEvent v : violations) {
            java.util.regex.Matcher m = CHAR_COUNT_PATTERN.matcher(v.getMessage());
            assertTrue(m.find(), "char count: " + v.getMessage());
            int len = Integer.parseInt(m.group(1));
            assertTrue(len <= MAX_LINE_LENGTH, "within max: " + v.getMessage());
            // Exact length assertions kill needsSpace mutations (L228, L232, L235)
            if (v.getLine() == MK2_LINE_CLOSING_BRACKET) {
                assertEquals(MK2_EXPECTED_CLOSING_LEN, len, "closingBracket: " + v.getMessage());
            }
            if (v.getLine() == MK2_LINE_OPENING_BRACKET) {
                assertEquals(MK2_EXPECTED_OPENING_LEN, len, "openingBracket: " + v.getMessage());
            }
            if (v.getLine() == MK2_LINE_SPACE_AT_END) {
                assertEquals(MK2_EXPECTED_SPACE_LEN, len, "spaceAtEnd: " + v.getMessage());
            }
        }
    }

    @Test
    void mutationKiller3NeedsSpacePrecision() throws Exception {
        // Targets remaining SURVIVED mutations:
        // L83: shouldSkip `type == RESOURCE && tryHeaderFitsOnOneLine(ast)` — 2S
        // L94: computeFirstLine `if (ident != null)` — 1S (CTOR_DEF has IDENT but no TYPE)
        // L106: firstNonAnnotationLine `if (modifiers != null)` — 1S
        // L172: tryHeaderFitsOnOneLine `spec == null || spec.getType() != RESOURCE_SPECIFICATION` — 2S
        // L191: findSignatureLastLine `if (semi != null)` — 1S
        // L217: buildCombinedLine `if (needsSpace(...))` — 1S
        // L228: needsSpace `if (sb.isEmpty())` — 1S
        // L232: needsSpace `CLOSING_BRACKETS || OPENING_BRACKETS` — 2S
        // L235: needsSpace `return last != ' '` — 1S
        List<AuditEvent> violations = runTreeWalkerCheck(UnnecessaryLineWrapCheck.class, "layout/invalid/UnnecessaryLineWrapMutationKiller3.java", DEFAULT_PROPS);

        // Resource wrapping (line 13)
        assertTrue(violations.stream().anyMatch(v -> v.getLine() == MK3_LINE_RESOURCE), "Resource wrapping should be detected: " + format(violations));

        // Constructor wrapping (line 23) - CTOR_DEF with no TYPE but has IDENT
        assertTrue(violations.stream().anyMatch(v -> v.getLine() == MK3_LINE_CTOR), "Constructor wrapping should be detected: " + format(violations));

        // Abstract method wrapping (line 50)
        assertTrue(violations.stream().anyMatch(v -> v.getLine() == MK3_LINE_ABSTRACT), "Abstract method wrapping should be detected: " + format(violations));

        // needsSpace precision: verify EXACT character counts per violation
        // If needsSpace mutations change space insertion, char counts change by 1
        for (AuditEvent v : violations) {
            java.util.regex.Matcher m = CHAR_COUNT_PATTERN.matcher(v.getMessage());
            assertTrue(m.find(), "char count: " + v.getMessage());
            // Assert specific lengths for specific lines to kill needsSpace mutations
            if (v.getLine() == MK3_LINE_RESULT) {
                // `String result = compute("test");` combined — needs space between "=" and "compute"
                assertEquals(MK3_EXPECTED_RESULT_LEN, Integer.parseInt(m.group(1)), "needsSpace must insert space correctly for result = compute: " + v.getMessage());
            }
        }
    }
}
