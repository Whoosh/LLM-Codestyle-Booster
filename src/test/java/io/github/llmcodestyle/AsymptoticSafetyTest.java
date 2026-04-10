package io.github.llmcodestyle;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;
import io.github.llmcodestyle.simplify.BooleanFromConditionCheck;
import io.github.llmcodestyle.simplify.CollapsibleConstantConcatenationCheck;
import io.github.llmcodestyle.simplify.CollapsibleGuardClauseCheck;
import io.github.llmcodestyle.simplify.CollapsibleNestedIfCheck;
import io.github.llmcodestyle.simplify.CollectionsToListOfCheck;
import io.github.llmcodestyle.simplify.ConditionalReturnToTernaryCheck;
import io.github.llmcodestyle.simplify.IdenticalCatchBodyCheck;
import io.github.llmcodestyle.simplify.IfReturnBooleanLiteralCheck;
import io.github.llmcodestyle.simplify.IndexOfToContainsCheck;
import io.github.llmcodestyle.simplify.InlineRegexConstantCheck;
import io.github.llmcodestyle.simplify.MapContainsKeyThenGetCheck;
import io.github.llmcodestyle.simplify.PureSingleUseLocalVariableCheck;
import io.github.llmcodestyle.simplify.RedundantConstantAliasCheck;
import io.github.llmcodestyle.simplify.SingleUseLocalVariableCheck;
import io.github.llmcodestyle.simplify.SplitDeclarationAssignmentCheck;
import io.github.llmcodestyle.simplify.StaticImportCandidateCheck;
import io.github.llmcodestyle.simplify.UseIsEmptyCheck;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies that our checks do not suggest fixes that would worsen asymptotic complexity.
 *
 * <p>The test resource {@code AsymptoticSafetyTraps.java} contains 38 trap patterns where
 * inlining a variable would move computation into a more-frequently-executed context
 * (for, for-each, while, do-while, lambda, stream, nested loops).
 *
 * <h3>Simplify checks with potential asymptotic risk (tested with traps):</h3>
 * <ul>
 *   <li>{@link SingleUseLocalVariableCheck} — guarded by {@code isInsideRepeatingContext}
 *       for loops, lambdas, and by {@code isInsideNestedBlock} for conditionals</li>
 *   <li>{@link PureSingleUseLocalVariableCheck} — guarded by {@code isUsedInRepeatingContext};
 *       pure-method whitelist prevents flagging expensive calls</li>
 * </ul>
 *
 * <h3>Simplify checks with NO asymptotic risk:</h3>
 * <ul>
 *   <li>{@link CollapsibleConstantConcatenationCheck} — merges compile-time constants,
 *       zero runtime impact (string concatenation of finals resolved by javac)</li>
 *   <li>{@link IndexOfToContainsCheck} — indexOf and contains are both O(n) for String;
 *       no asymptotic change, only readability improvement</li>
 *   <li>{@link InlineRegexConstantCheck} — extracting regex to static final IMPROVES
 *       performance (Pattern.compile called once instead of per-method-call)</li>
 *   <li>{@link StaticImportCandidateCheck} — import style only, zero bytecode difference</li>
 *   <li>{@link UseIsEmptyCheck} — isEmpty() and length()/size() are both O(1);
 *       no asymptotic change, only readability improvement</li>
 *   <li>{@link IdenticalCatchBodyCheck} — merges identical catch clauses,
 *       zero runtime impact (control flow only)</li>
 *   <li>{@link MapContainsKeyThenGetCheck} — replaces double lookup with single,
 *       actually IMPROVES performance (fewer hash computations)</li>
 *   <li>{@link CollectionsToListOfCheck} — factory method replacement,
 *       no asymptotic change (both O(n) for n elements)</li>
 *   <li>{@link ConditionalReturnToTernaryCheck} — syntactic sugar only,
 *       zero bytecode difference in return paths</li>
 *   <li>{@link CollapsibleGuardClauseCheck} — collapses guard + conditional into a single
 *       boolean expression, identical bytecode after javac optimization</li>
 *   <li>{@link CollapsibleNestedIfCheck} — merges two boolean conditions with {@code &&},
 *       same short-circuit semantics, no asymptotic change</li>
 *   <li>{@link BooleanFromConditionCheck} — replaces if-flip with direct assignment,
 *       evaluates the condition exactly once in both forms</li>
 *   <li>{@link SplitDeclarationAssignmentCheck} — moves declaration to its initializer,
 *       no runtime impact</li>
 *   <li>{@link IfReturnBooleanLiteralCheck} — collapses if-return-literal pair to a single
 *       return of the condition, identical control flow</li>
 *   <li>{@link RedundantConstantAliasCheck} — flags useless static final aliases and
 *       same-class duplicate Pattern.compile, replacing two field reads with one</li>
 * </ul>
 */
class AsymptoticSafetyTest {

    private static final String TRAPS_FILE = "valid/AsymptoticSafetyTraps.java";

    @Test
    void singleUseCheckDoesNotFlagLoopCachedVariables() throws Exception {
        List<AuditEvent> violations = TestCheckSupport.runTreeWalkerCheck(SingleUseLocalVariableCheck.class, TRAPS_FILE, Map.of());
        assertTrue(violations.isEmpty(), "SingleUseLocalVariableCheck must not flag loop-cached variables (" + violations.size() + " violations): " + format(violations));
    }

    @Test
    void pureSingleUseCheckDoesNotFlagLoopCachedVariables() throws Exception {
        List<AuditEvent> violations = TestCheckSupport.runTreeWalkerCheck(PureSingleUseLocalVariableCheck.class, TRAPS_FILE, Map.of());
        assertTrue(violations.isEmpty(), "PureSingleUseLocalVariableCheck must not flag loop-cached variables (" + violations.size() + " violations): " + format(violations));
    }

    @Test
    void bothChecksTogetherProduceZeroOnTraps() throws Exception {
        List<AuditEvent> violations = TestCheckSupport.runMultipleTreeWalkerChecks(
            Map.of(
                SingleUseLocalVariableCheck.class.getName(),
                Map.of(),
                PureSingleUseLocalVariableCheck.class.getName(),
                Map.of()),
            TRAPS_FILE);
        assertTrue(violations.isEmpty(), "Combined single-use checks must not flag any of 38 asymptotic traps (" + violations.size() + " violations): " + format(violations));
    }

    private static String format(List<AuditEvent> events) {
        return events.stream()
            .map(e -> "Line " + e.getLine() + ": " + e.getMessage())
            .toList()
            .toString();
    }
}
