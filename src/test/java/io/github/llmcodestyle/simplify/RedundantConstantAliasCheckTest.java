package io.github.llmcodestyle.simplify;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.github.llmcodestyle.utils.TestCheckSupportUtil.*;
import static org.junit.jupiter.api.Assertions.*;

class RedundantConstantAliasCheckTest {

    private static final Map<String, String> NO_PROPS = Map.of();
    private static final int EXPECTED_VIOLATIONS = 5;

    @Test
    void aliasesAndDuplicatePatternsAreFlagged() throws Exception {
        List<AuditEvent> violations = run("simplify/invalid/RedundantConstantAliasInvalid.java");
        assertEquals(EXPECTED_VIOLATIONS, violations.size(), "Expected 5 violations: " + format(violations));
    }

    @Test
    void uniqueConstantsAndOneOffPatternsProduceNoViolations() throws Exception {
        assertTrue(run("simplify/valid/RedundantConstantAliasValid.java").isEmpty());
    }

    @Test
    void violationMessageContainsConstantName() throws Exception {
        List<AuditEvent> violations = run("simplify/invalid/RedundantConstantAliasInvalid.java");
        for (AuditEvent event : violations) {
            assertNotNull(event.getMessage(), "Message should not be null");
            assertFalse(event.getMessage().isEmpty(), "Message should not be empty");
            assertTrue(event.getLine() > 0, "Line should be positive");
        }
    }

    @Test
    void patternEdgeCasesProduceViolations() throws Exception {
        List<AuditEvent> violations = run("simplify/invalid/RedundantConstantAliasPatternEdge.java");
        // PAT_A_DUP (dup of PAT_A via constant), PAT_B_DUP (dup of PAT_B via literal),
        // ALIAS_OF_A (alias of REGEX_A), TOKEN_ALIAS (alias of TOKEN in interface)
        assertTrue(violations.size() >= 4,
            "Expected at least 4 violations in pattern edge cases: " + format(violations));
    }

    @Test
    void patternCompileDuplicateViaConstantReferenceDetected() throws Exception {
        List<AuditEvent> violations = run("simplify/invalid/RedundantConstantAliasPatternEdge.java");
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("PAT_A_DUP")),
            "Duplicate Pattern via constant ref should be flagged: " + format(violations));
    }

    @Test
    void patternCompileDuplicateViaLiteralDetected() throws Exception {
        List<AuditEvent> violations = run("simplify/invalid/RedundantConstantAliasPatternEdge.java");
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("PAT_B_DUP")),
            "Duplicate Pattern via literal should be flagged: " + format(violations));
    }

    @Test
    void simpleIdentAliasDetected() throws Exception {
        List<AuditEvent> violations = run("simplify/invalid/RedundantConstantAliasPatternEdge.java");
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("ALIAS_OF_A")),
            "Simple ident alias should be flagged: " + format(violations));
    }

    @Test
    void interfaceFieldAliasDetected() throws Exception {
        List<AuditEvent> violations = run("simplify/invalid/RedundantConstantAliasPatternEdge.java");
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("TOKEN_ALIAS")),
            "Interface field alias should be flagged: " + format(violations));
    }

    @Test
    void originalViolationsContainSpecificNames() throws Exception {
        List<AuditEvent> violations = run("simplify/invalid/RedundantConstantAliasInvalid.java");
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("HI")),
            "Should flag HI as alias: " + format(violations));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("PROBLEM_START")),
            "Should flag PROBLEM_START as alias: " + format(violations));
    }

    private static List<AuditEvent> run(String resource) throws Exception {
        return runTreeWalkerCheck(RedundantConstantAliasCheck.class, resource, NO_PROPS);
    }
}
