package io.github.llmcodestyle.quality;

import io.github.llmcodestyle.TestCheckSupport;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DuplicateRegexConstantCheckTest {

    private static final Map<String, String> NO_PROPS = Map.of();

    private static final String INVALID_A = "quality/invalid/DuplicateRegexConstantInvalidA.java";
    private static final String INVALID_B = "quality/invalid/DuplicateRegexConstantInvalidB.java";
    private static final String INVALID_C = "quality/invalid/DuplicateRegexConstantInvalidC.java";
    private static final String INVALID_D = "quality/invalid/DuplicateRegexConstantInvalidD.java";
    private static final String INVALID_WITHIN = "quality/invalid/DuplicateRegexConstantInvalidWithinFile.java";
    private static final String VALID = "quality/valid/DuplicateRegexConstantValid.java";
    private static final String VALID_FP = "quality/valid/DuplicateRegexConstantValidFalsePositives.java";

    @Test
    void duplicateStringAndPatternAcrossTwoFiles() throws Exception {
        List<AuditEvent> violations = runMulti(INVALID_A, INVALID_B);
        assertEquals(2, violations.size(),
            "Expected EMAIL_REGEX + DIGITS duplicates: " + format(violations));
    }

    @Test
    void duplicatesAccumulateAcrossThreeFiles() throws Exception {
        List<AuditEvent> violations = runMulti(INVALID_A, INVALID_B, INVALID_C);
        assertEquals(
            5, violations.size(),
            "A→0, B→2 (email+digits), C→3 (date+ws+range): " + format(violations));
    }

    @Test
    void duplicatesAcrossFourFilesIncludingEnum() throws Exception {
        List<AuditEvent> violations = runMulti(INVALID_A, INVALID_B, INVALID_C, INVALID_D);
        assertEquals(
            6, violations.size(),
            "A→0, B→2, C→3, D→1 (ip): " + format(violations));
    }

    @Test
    void duplicateDetectedInRecord() throws Exception {
        List<AuditEvent> violations = runMulti(INVALID_A, INVALID_C);
        assertEquals(
            2, violations.size(),
            "Record should flag DATE_PATTERN + WHITESPACE dups from A: " + format(violations));
    }

    @Test
    void duplicateDetectedInEnum() throws Exception {
        List<AuditEvent> violations = runMulti(INVALID_A, INVALID_D);
        assertEquals(
            1, violations.size(),
            "Enum should flag IP_REGEX dup from A: " + format(violations));
    }

    @Test
    void duplicateWithinSameFile() throws Exception {
        List<AuditEvent> violations = runSingle(INVALID_WITHIN);
        assertEquals(
            2, violations.size(),
            "Same-file dups for VERSION and ASSIGN: " + format(violations));
    }

    @Test
    void uniqueRegexConstantsProduceNoViolations() throws Exception {
        List<AuditEvent> violations = runSingle(VALID);
        assertTrue(violations.isEmpty(),
            "Unique regex constants should not trigger: " + format(violations));
    }

    @Test
    void falsePositivesProduceNoViolations() throws Exception {
        List<AuditEvent> violations = runSingle(VALID_FP);
        assertTrue(violations.isEmpty(),
            "False positives (paths, escapes, non-static/final, concat, plain) must not trigger: "
                + format(violations));
    }

    @Test
    void singleInvalidFileAloneHasNoDuplicates() throws Exception {
        List<AuditEvent> violations = runSingle(INVALID_A);
        assertTrue(violations.isEmpty(),
            "Single file has no cross-file duplicates: " + format(violations));
    }

    @Test
    void violationMessageReferencesOriginalConstantAndClass() throws Exception {
        List<AuditEvent> violations = runMulti(INVALID_A, INVALID_B);
        boolean refsEmail = violations.stream()
            .anyMatch(v -> v.getMessage().contains("EMAIL_REGEX")
                && v.getMessage().contains("DuplicateRegexConstantInvalidA"));
        boolean refsDigits = violations.stream()
            .anyMatch(v -> v.getMessage().contains("DIGITS")
                && v.getMessage().contains("DuplicateRegexConstantInvalidA"));
        assertTrue(refsEmail, "Should reference EMAIL_REGEX in A: " + format(violations));
        assertTrue(refsDigits, "Should reference DIGITS in A: " + format(violations));
    }

    @Test
    void falsePositivePathsNotContaminatingCrossFileResults() throws Exception {
        List<AuditEvent> violations = runMulti(VALID_FP, INVALID_A);
        assertTrue(violations.isEmpty(),
            "FP file has no real regex detected, A has only first occurrences: "
                + format(violations));
    }

    private static List<AuditEvent> runSingle(String resource) throws Exception {
        return TestCheckSupport.runTreeWalkerCheck(
            DuplicateRegexConstantCheck.class, resource, NO_PROPS);
    }

    private static List<AuditEvent> runMulti(String... resources) throws Exception {
        return TestCheckSupport.runTreeWalkerCheckMultiFile(
            DuplicateRegexConstantCheck.class, List.of(resources), NO_PROPS);
    }

    private static String format(List<AuditEvent> events) {
        return events.stream()
            .map(e -> "\n  Line " + e.getLine() + " [" + e.getFileName() + "]: " + e.getMessage())
            .toList()
            .toString();
    }
}
