package io.github.llmcodestyle.simplify;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.github.llmcodestyle.utils.TestCheckSupportUtil.*;
import static org.junit.jupiter.api.Assertions.*;

class IfReturnBooleanLiteralCheckTest {

    private static final Map<String, String> NO_PROPS = Map.of();
    private static final int EXPECTED_VIOLATIONS = 4;

    @Test
    void ifReturnBooleansProduceViolations() throws Exception {
        assertEquals(EXPECTED_VIOLATIONS, run("simplify/invalid/IfReturnBooleanLiteralInvalid.java").size());
    }

    @Test
    void validCornerCasesProduceNoViolations() throws Exception {
        assertTrue(run("simplify/valid/IfReturnBooleanLiteralValid.java").isEmpty());
    }

    @Test
    void semiPathDetection() throws Exception {
        // followingReturnInBlock line 78: siblingType != SEMI && siblingType != RCURLY
        // The SLIST has children: LITERAL_IF, then possibly SEMI/RCURLY, then LITERAL_RETURN
        List<AuditEvent> violations = run("simplify/invalid/IfReturnBooleanLiteralSemiPath.java");
        assertEquals(1, violations.size(),
            "Should detect if-return-boolean with RCURLY siblings: " + format(violations));
    }

    @Test
    void exactViolationCount() throws Exception {
        List<AuditEvent> violations = run("simplify/invalid/IfReturnBooleanLiteralInvalid.java");
        assertEquals(EXPECTED_VIOLATIONS, violations.size(),
            "Exact violation count: " + format(violations));
        for (AuditEvent v : violations) {
            assertTrue(v.getLine() > 0, "Line should be positive");
        }
    }

    @Test
    void followingReturnSkipsSemiAndRcurly() throws Exception {
        // L78 SURVIVED: `siblingType != SEMI && siblingType != RCURLY`
        // If negated: SEMI/RCURLY would NOT be skipped, returning null and missing the violation.
        List<AuditEvent> violations = run("simplify/invalid/IfReturnBoolLitSemiKill.java");
        assertEquals(1, violations.size(),
            "Should find the if-return-boolean pattern despite SEMI/RCURLY siblings: " + format(violations));
    }

    private static List<AuditEvent> run(String resource) throws Exception {
        return runTreeWalkerCheck(IfReturnBooleanLiteralCheck.class, resource, NO_PROPS);
    }
}
