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

    @Test
    void mutationKillerValidProducesNoViolations() throws Exception {
        // Exercises: two-arg Pattern.compile, non-static-final field, static-only field,
        // no ASSIGN field, computed initializer, concat initializer for Pattern.compile
        List<AuditEvent> violations = run("simplify/valid/RedundantConstantAliasMutationKiller.java");
        assertTrue(violations.isEmpty(),
            "Mutation killer valid fixture should produce no violations: " + format(violations));
    }

    @Test
    void isEffectivelyStaticFinalDistinguishesModifiers() throws Exception {
        // The valid fixture has non-static-final and static-only fields that must NOT be flagged
        List<AuditEvent> violations = run("simplify/valid/RedundantConstantAliasMutationKiller.java");
        assertTrue(violations.stream().noneMatch(v -> v.getMessage().contains("notStaticFinal")),
            "Non-static-final field should not be flagged: " + format(violations));
        assertTrue(violations.stream().noneMatch(v -> v.getMessage().contains("staticOnly")),
            "Static-only (not final) field should not be flagged: " + format(violations));
    }

    @Test
    void patternCompileWithTwoArgsNotFlagged() throws Exception {
        List<AuditEvent> violations = run("simplify/valid/RedundantConstantAliasMutationKiller.java");
        assertTrue(violations.stream().noneMatch(v -> v.getMessage().contains("TWO_ARG")),
            "Pattern.compile with two args should not be flagged: " + format(violations));
    }

    @Test
    void patternCompileConcatNotFlagged() throws Exception {
        List<AuditEvent> violations = run("simplify/valid/RedundantConstantAliasMutationKiller.java");
        assertTrue(violations.stream().noneMatch(v -> v.getMessage().contains("FROM_EXPR")),
            "Pattern.compile with concatenated arg should not be flagged: " + format(violations));
    }

    @Test
    void simpleIdentInitializerReturnsNullForNonIdentExpr() throws Exception {
        // COMPUTED field uses String.valueOf(42) as initializer — not a simple IDENT
        List<AuditEvent> violations = run("simplify/valid/RedundantConstantAliasMutationKiller.java");
        assertTrue(violations.stream().noneMatch(v -> v.getMessage().contains("COMPUTED")),
            "Non-IDENT initializer should not be treated as alias: " + format(violations));
    }

    @Test
    void mutationKillerInvalidProducesExpectedViolations() throws Exception {
        List<AuditEvent> violations = run("simplify/invalid/RedundantConstantAliasMutationKiller.java");
        // PAT_X_DUP (dup pattern via constant ref), ALIAS_X (alias), PAT_LITERAL_DUP (dup pattern via literal), GREETING_ALIAS (alias)
        assertEquals(4, violations.size(),
            "Mutation killer should produce 4 violations: " + format(violations));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("PAT_X_DUP")),
            "Duplicate pattern via constant ref: " + format(violations));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("ALIAS_X")),
            "Simple ident alias: " + format(violations));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("PAT_LITERAL_DUP")),
            "Duplicate pattern via literal: " + format(violations));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("GREETING_ALIAS")),
            "String alias: " + format(violations));
    }

    @Test
    void mutationKiller2ValidProducesNoViolations() throws Exception {
        List<AuditEvent> violations = run("simplify/valid/RedundantConstantAliasMutationKiller2.java");
        assertTrue(violations.isEmpty(),
            "Mutation killer 2 valid fixture should produce no violations: " + format(violations));
    }

    @Test
    void patternCompileStringLiteralPath() throws Exception {
        // PAT_LITERAL = Pattern.compile("\\d+") — patternCompileValue STRING_LITERAL branch (line 167)
        List<AuditEvent> violations = run("simplify/invalid/RedundantConstantAliasMutationKiller.java");
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("PAT_LITERAL_DUP")),
            "Pattern compile with string literal arg should be detected: " + format(violations));
    }

    @Test
    void patternCompileIdentPath() throws Exception {
        // PAT_X_DUP = Pattern.compile(REGEX_X) — patternCompileValue IDENT branch (line 170)
        List<AuditEvent> violations = run("simplify/invalid/RedundantConstantAliasMutationKiller.java");
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("PAT_X_DUP")),
            "Pattern compile with constant ident arg should be detected: " + format(violations));
    }

    private static List<AuditEvent> run(String resource) throws Exception {
        return runTreeWalkerCheck(RedundantConstantAliasCheck.class, resource, NO_PROPS);
    }
}
