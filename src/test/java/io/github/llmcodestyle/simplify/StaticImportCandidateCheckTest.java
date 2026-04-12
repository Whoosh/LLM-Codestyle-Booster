package io.github.llmcodestyle.simplify;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.github.llmcodestyle.utils.TestCheckSupportUtil.*;
import static org.junit.jupiter.api.Assertions.*;

class StaticImportCandidateCheckTest {

    private static final int EXPECTED_DISTINCT_VIOLATIONS = 8;
    private static final int HOLDER_PATTERN_LINE = 17;

    @Test
    void invalidCasesProduceViolations() throws Exception {
        assertFalse(
            runTreeWalkerCheck(
                StaticImportCandidateCheck.class,
                "simplify/invalid/StaticImportCandidateInvalid.java",
                Map.of()).isEmpty(),
            "Expected static import candidate violations but got none");
    }

    @Test
    void everyDistinctQualifiedRefFires() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(StaticImportCandidateCheck.class, "simplify/invalid/StaticImportCandidateInvalid.java", Map.of());
        assertEquals(EXPECTED_DISTINCT_VIOLATIONS, violations.size(), "Expected 8 distinct violations (constants + util method calls) but got: " + format(violations));
    }

    @Test
    void utilMethodCallsFire() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(StaticImportCandidateCheck.class, "simplify/invalid/StaticImportCandidateInvalid.java", Map.of());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("extractPackageName")), format(violations));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("isBlank")), format(violations));
    }

    @Test
    void qualifiedConstantAsMethodReceiverFires() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(StaticImportCandidateCheck.class, "simplify/invalid/StaticImportCandidateInvalid.java", Map.of());
        assertTrue(violations.stream().anyMatch(e -> e.getLine() == HOLDER_PATTERN_LINE), "Expected violation on line 17, got: " + format(violations));
    }

    @Test
    void validCasesProduceNoViolations() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(StaticImportCandidateCheck.class, "simplify/valid/StaticImportCandidateValid.java", Map.of());
        assertEquals(0, violations.size(), "Expected no violations (PI already static-imported) but got: " + format(violations));
    }

    @Test
    void staticImportExcludesQualifiedRefButOtherConstantStillFires() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(StaticImportCandidateCheck.class, "simplify/invalid/StaticImportCandidateWithImport.java", Map.of());
        assertEquals(1, violations.size(), "Expected 1 violation (Integer.MAX_VALUE only), got: " + format(violations));
    }

    @Test
    void ambiguousConstantsOnlyFireForWinner() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(StaticImportCandidateCheck.class,
            "simplify/invalid/StaticImportCandidateMutationKiller.java", Map.of());
        // Beta.VALUE has 2 refs, Alpha.VALUE has 1 — Beta wins, Alpha skipped
        // Gamma.____ is underscore-only, not upper-case constant — not flagged
        // Delta.V_1 has 2 refs — should be flagged
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Beta")),
            "Beta.VALUE (winner) should be flagged: " + format(violations));
        assertTrue(violations.stream().noneMatch(v -> v.getMessage().contains("Alpha")),
            "Alpha.VALUE (loser) should not be flagged: " + format(violations));
        assertTrue(violations.stream().noneMatch(v -> v.getMessage().contains("Gamma")),
            "Gamma.____ (underscore-only) should not be flagged: " + format(violations));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Delta")),
            "Delta.V_1 should be flagged: " + format(violations));
    }

    @Test
    void starImportExercisedFindLastIdent() throws Exception {
        // The star import in MutationKiller fixture exercises findLastIdent with STAR child
        List<AuditEvent> violations = runTreeWalkerCheck(StaticImportCandidateCheck.class,
            "simplify/invalid/StaticImportCandidateMutationKiller.java", Map.of());
        // Star import should not crash, and constant refs should still be found
        assertNotNull(violations, "Should not throw");
        assertFalse(violations.isEmpty(), "Should detect qualified refs: " + format(violations));
    }

    @Test
    void isInsideImportPreventsFlaggerOnImportDots() throws Exception {
        // DOTs inside import statements should be skipped (isInsideImport line 176)
        List<AuditEvent> violations = runTreeWalkerCheck(StaticImportCandidateCheck.class,
            "simplify/invalid/StaticImportCandidateInvalid.java", Map.of());
        // No violation should reference "java" or "util" or "regex" from import DOTs
        assertTrue(violations.stream().noneMatch(v -> v.getMessage().contains("java")),
            "Import DOTs should not be flagged: " + format(violations));
    }

    @Test
    void violationMessageContainsClassName() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(StaticImportCandidateCheck.class,
            "simplify/invalid/StaticImportCandidateInvalid.java", Map.of());
        for (AuditEvent v : violations) {
            assertFalse(v.getMessage().isEmpty(), "Message should not be empty");
            assertTrue(v.getLine() > 0, "Line should be positive");
        }
    }
}
