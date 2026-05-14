package io.github.llmcodestyle;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies that all custom check classes return non-null, non-empty token arrays
 * from getDefaultTokens, getAcceptableTokens, and getRequiredTokens. This kills
 * NULL_RETURNS and EMPTY_RETURNS mutations on these methods.
 */
class TokenArraySanityTest {

    @Test
    void layoutChecksReturnValidTokenArrays() {
        verifyTokenArrays(new io.github.llmcodestyle.layout.ArrayInitSpaceCheck());
        verifyTokenArrays(new io.github.llmcodestyle.layout.ChainedCallLineBreakCheck());
        verifyTokenArrays(new io.github.llmcodestyle.layout.CompactableParameterListCheck());
        verifyTokenArrays(new io.github.llmcodestyle.layout.MethodCallArgumentsOnSameLineCheck());
        verifyTokenArrays(new io.github.llmcodestyle.layout.StaticFinalFirstCheck());
        verifyTokenArrays(new io.github.llmcodestyle.layout.StaticStarImportCheck());
        verifyTokenArrays(new io.github.llmcodestyle.layout.UnnecessaryLineWrapCheck());
    }

    @Test
    void forbiddenChecksReturnValidTokenArrays() {
        verifyTokenArrays(new io.github.llmcodestyle.forbidden.ForbidAssertKeywordCheck());
        verifyTokenArrays(new io.github.llmcodestyle.forbidden.ForbiddenGenericCatchCheck());
        verifyTokenArrays(new io.github.llmcodestyle.forbidden.NoSuppressionCheck());
        verifyTokenArrays(new io.github.llmcodestyle.forbidden.NoSystemOutInProductionCheck());
    }

    @Test
    void simplifyChecksReturnValidTokenArrays() {
        verifyTokenArrays(new io.github.llmcodestyle.simplify.BooleanFromConditionCheck());
        verifyTokenArrays(new io.github.llmcodestyle.simplify.CollapsibleConsecutiveIfCheck());
        verifyTokenArrays(new io.github.llmcodestyle.simplify.CollapsibleConstantConcatenationCheck());
        verifyTokenArrays(new io.github.llmcodestyle.simplify.CollapsibleGuardClauseCheck());
        verifyTokenArrays(new io.github.llmcodestyle.simplify.CollapsibleNestedIfCheck());
        verifyTokenArrays(new io.github.llmcodestyle.simplify.CollectionsToListOfCheck());
        verifyTokenArrays(new io.github.llmcodestyle.simplify.CommonsLang3StringConstantCheck());
        verifyTokenArrays(new io.github.llmcodestyle.simplify.ConditionalReturnToTernaryCheck());
        verifyTokenArrays(new io.github.llmcodestyle.simplify.IdenticalCatchBodyCheck());
        verifyTokenArrays(new io.github.llmcodestyle.simplify.IfReturnBooleanLiteralCheck());
        verifyTokenArrays(new io.github.llmcodestyle.simplify.IndexOfToContainsCheck());
        verifyTokenArrays(new io.github.llmcodestyle.simplify.InlineRegexConstantCheck());
        verifyTokenArrays(new io.github.llmcodestyle.simplify.MapContainsKeyThenGetCheck());
        verifyTokenArrays(new io.github.llmcodestyle.simplify.OrChainToSetContainsCheck());
        verifyTokenArrays(new io.github.llmcodestyle.simplify.PureSingleUseLocalVariableCheck());
        verifyTokenArrays(new io.github.llmcodestyle.simplify.RedundantConstantAliasCheck());
        verifyTokenArrays(new io.github.llmcodestyle.simplify.SingleUseLocalVariableCheck());
        verifyTokenArrays(new io.github.llmcodestyle.simplify.SplitDeclarationAssignmentCheck());
        verifyTokenArrays(new io.github.llmcodestyle.simplify.StaticImportCandidateCheck());
        verifyTokenArrays(new io.github.llmcodestyle.simplify.TrivialSingleUsePrivateMethodCheck());
        verifyTokenArrays(new io.github.llmcodestyle.simplify.UseIsEmptyCheck());
    }

    @Test
    void qualityChecksReturnValidTokenArrays() {
        verifyTokenArrays(new io.github.llmcodestyle.quality.checks.ClassMayBeRecordCheck());
        verifyTokenArrays(new io.github.llmcodestyle.quality.checks.DuplicateMethodBodyCheck());
        verifyTokenArrays(new io.github.llmcodestyle.quality.checks.DuplicateRegexConstantCheck());
        verifyTokenArrays(new io.github.llmcodestyle.quality.checks.ExplicitNullReturnCheck());
        verifyTokenArrays(new io.github.llmcodestyle.quality.checks.LongTestLiteralCheck());
        verifyTokenArrays(new io.github.llmcodestyle.quality.checks.MethodMayBeStaticCheck());
        verifyTokenArrays(new io.github.llmcodestyle.quality.checks.MissingNullableParameterCheck());
        verifyTokenArrays(new io.github.llmcodestyle.quality.checks.PackageGroupingByClassSuffixCheck());
        verifyTokenArrays(new io.github.llmcodestyle.quality.checks.PublicMethodTestCoverageCheck());
        verifyTokenArrays(new io.github.llmcodestyle.quality.checks.RepeatedExceptionWrappingCheck());
        verifyTokenArrays(new io.github.llmcodestyle.quality.checks.SpringBootMainVisibilityCheck());
        verifyTokenArrays(new io.github.llmcodestyle.quality.checks.TestClassNamingCheck());
        verifyTokenArrays(new io.github.llmcodestyle.quality.checks.TestMethodNameCheck());
        verifyTokenArrays(new io.github.llmcodestyle.quality.checks.TestOnlyDelegateCheck());
        verifyTokenArrays(new io.github.llmcodestyle.quality.checks.TopLevelEnumInEnumsPackageCheck());
        verifyTokenArrays(new io.github.llmcodestyle.quality.checks.TopLevelRecordInPojosPackageCheck());
        verifyTokenArrays(new io.github.llmcodestyle.quality.checks.UnrelatedNestedClassCheck());
        verifyTokenArrays(new io.github.llmcodestyle.quality.checks.UnrelatedNestedEnumCheck());
        verifyTokenArrays(new io.github.llmcodestyle.quality.checks.UnrelatedNestedInterfaceCheck());
        verifyTokenArrays(new io.github.llmcodestyle.quality.checks.UnrelatedNestedRecordCheck());
        verifyTokenArrays(new io.github.llmcodestyle.quality.checks.UnusedPrivateMembersCheck());
        verifyTokenArrays(new io.github.llmcodestyle.quality.checks.UtilClassInUtilsPackageCheck());
        verifyTokenArrays(new io.github.llmcodestyle.quality.checks.UtilClassNamingCheck());
    }

    private static void verifyTokenArrays(AbstractCheck check) {
        String name = check.getClass().getSimpleName();
        int[] defaultTokens = check.getDefaultTokens();
        int[] acceptableTokens = check.getAcceptableTokens();
        int[] requiredTokens = check.getRequiredTokens();

        assertNotNull(defaultTokens, name + ": getDefaultTokens() returned null");
        assertNotNull(acceptableTokens, name + ": getAcceptableTokens() returned null");
        assertNotNull(requiredTokens, name + ": getRequiredTokens() returned null");
        assertTrue(defaultTokens.length > 0, name + ": getDefaultTokens() returned empty array");
        assertTrue(acceptableTokens.length > 0, name + ": getAcceptableTokens() returned empty array");
        assertTrue(requiredTokens.length > 0, name + ": getRequiredTokens() returned empty array");

        // Default and acceptable should be the same (convention in this project)
        assertArrayEquals(defaultTokens, acceptableTokens, name + ": getDefaultTokens and getAcceptableTokens should match");
    }
}
