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

    private static String format(List<AuditEvent> violations) {
        return violations.stream()
            .map(e -> "Line " + e.getLine() + ": " + e.getMessage())
            .toList()
            .toString();
    }
}
