package io.github.llmcodestyle.simplify;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.github.llmcodestyle.utils.TestCheckSupportUtil.*;
import static org.junit.jupiter.api.Assertions.*;

class IdenticalCatchBodyCheckTest {

    private static final Map<String, String> NO_PROPS = Map.of();
    private static final int EXPECTED_VIOLATIONS = 2;
    private static final int EXPECTED_EDGE_CASE_VIOLATIONS = 5;
    private static final int EXPECTED_IDENTICAL_LITERAL_VIOLATIONS = 4;
    private static final int SAME_DOUBLE_FIRST_CATCH_LINE = 10;
    private static final int SAME_DOUBLE_SECOND_CATCH_LINE = 12;
    private static final int SAME_BOOLEAN_FIRST_CATCH_LINE = 21;
    private static final int SAME_BOOLEAN_SECOND_CATCH_LINE = 23;

    @Test
    void identicalCatchBodiesProduceViolations() throws Exception {
        List<AuditEvent> violations = runCheck("simplify/invalid/IdenticalCatchBodyInvalid.java");
        assertEquals(EXPECTED_VIOLATIONS, violations.size(), "Expected 2 identical catch body violations: " + format(violations));
    }

    @Test
    void differentCatchBodiesProduceNoViolations() throws Exception {
        assertTrue(runCheck("simplify/valid/IdenticalCatchBodyValid.java").isEmpty());
    }

    @Test
    void threeCatchesWithSameBodyAllFlagged() throws Exception {
        List<AuditEvent> violations = runCheck("simplify/invalid/IdenticalCatchBodyEdgeCases.java");
        // threeCatches: 3 identical catches, differentVarNames: 2 identical catches = 5 total
        // differentLiterals: not flagged (different string literals)
        assertTrue(
            violations.size() >= EXPECTED_EDGE_CASE_VIOLATIONS,
            "Three identical catches + two different-var-name catches should produce 5+ violations: " + format(violations));
    }

    @Test
    void differentVarNamesSameBodyStructureIsFlagged() throws Exception {
        List<AuditEvent> violations = runCheck("simplify/invalid/IdenticalCatchBodyEdgeCases.java");
        // The differentVarNames method should have 2 violations (catches with different names but same structure)
        assertTrue(violations.size() >= 2, "Catches with different variable names but same body should be flagged: " + format(violations));
    }

    @Test
    void differentLiteralsAreNotFlagged() throws Exception {
        List<AuditEvent> violations = runCheck("simplify/invalid/IdenticalCatchBodyEdgeCases.java");
        // Only threeCatches (3) and differentVarNames (2) should be flagged, not differentLiterals
        assertEquals(EXPECTED_EDGE_CASE_VIOLATIONS, violations.size(), "Different string literals should not be treated as identical: " + format(violations));
    }

    @Test
    void violationsReportCatchLineNumbers() throws Exception {
        for (AuditEvent event : runCheck("simplify/invalid/IdenticalCatchBodyInvalid.java")) {
            assertTrue(event.getLine() > 0, "Violation should report a positive line number");
        }
    }

    @Test
    void buildFingerprintDistinguishesByIdentName() throws Exception {
        // buildFingerprint line 86: if IDENT, append exception var text
        // If negated, all catches with same structure but different var names would hash differently
        List<AuditEvent> violations = runCheck("simplify/invalid/IdenticalCatchBodyEdgeCases.java");
        // differentVarNames method has catches: catch(A ex) and catch(B e) with same body
        // The fingerprint normalizes the exception var so they should be identical
        assertTrue(violations.size() >= 2, "Catches with different exception var names should be treated as identical: " + format(violations));
    }

    @Test
    void differentMethodCallsInCatchBodiesAreNotIdentical() throws Exception {
        // buildFingerprint IDENT check: if negated, method name differences are lost
        List<AuditEvent> violations = runCheck("simplify/valid/IdenticalCatchBodyMutKiller.java");
        assertTrue(violations.isEmpty(), "Catches calling different methods should not be treated as identical: " + format(violations));
    }

    @Test
    void differentNonStringLiteralsAreNotFlagged() throws Exception {
        // Regression: floating-point, boolean, char, and null literals must be part of the
        // fingerprint so that catches differing only in those literals are NOT treated as identical.
        List<AuditEvent> violations = runCheck("simplify/valid/IdenticalCatchBodyDifferentLiteralsValid.java");
        assertTrue(violations.isEmpty(), "Catches differing only in double/float/boolean/char/null literals must not be flagged: " + format(violations));
    }

    @Test
    void identicalNonStringLiteralBodiesAreStillFlagged() throws Exception {
        // Regression guard: when the literal values truly match, the check must still fire.
        List<AuditEvent> violations = runCheck("simplify/invalid/IdenticalCatchBodyIdenticalLiteralsInvalid.java");
        assertEquals(
            EXPECTED_IDENTICAL_LITERAL_VIOLATIONS,
            violations.size(),
            "Two pairs of truly-identical catches (double + boolean) should produce 4 violations: " + format(violations));
        assertHasViolationAtLine(violations, SAME_DOUBLE_FIRST_CATCH_LINE, "sameDoubleLiteral first catch");
        assertHasViolationAtLine(violations, SAME_DOUBLE_SECOND_CATCH_LINE, "sameDoubleLiteral second catch");
        assertHasViolationAtLine(violations, SAME_BOOLEAN_FIRST_CATCH_LINE, "sameBooleanLiteral first catch");
        assertHasViolationAtLine(violations, SAME_BOOLEAN_SECOND_CATCH_LINE, "sameBooleanLiteral second catch");
    }

    private static void assertHasViolationAtLine(List<AuditEvent> violations, int line, String description) {
        assertTrue(violations.stream().anyMatch(e -> e.getLine() == line), String.format("Expected violation at line %d (%s): %s", line, description, format(violations)));
    }

    private static List<AuditEvent> runCheck(String resource) throws Exception {
        return runTreeWalkerCheck(IdenticalCatchBodyCheck.class, resource, NO_PROPS);
    }
}
