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
        verifyTokenArrays(new io.github.llmcodestyle.quality.ClassMayBeRecordCheck());
        verifyTokenArrays(new io.github.llmcodestyle.quality.DuplicateMethodBodyCheck());
        verifyTokenArrays(new io.github.llmcodestyle.quality.DuplicateRegexConstantCheck());
        verifyTokenArrays(new io.github.llmcodestyle.quality.ExplicitNullReturnCheck());
        verifyTokenArrays(new io.github.llmcodestyle.quality.LongTestLiteralCheck());
        verifyTokenArrays(new io.github.llmcodestyle.quality.MethodMayBeStaticCheck());
        verifyTokenArrays(new io.github.llmcodestyle.quality.MissingNullableParameterCheck());
        verifyTokenArrays(new io.github.llmcodestyle.quality.PublicMethodTestCoverageCheck());
        verifyTokenArrays(new io.github.llmcodestyle.quality.RepeatedExceptionWrappingCheck());
        verifyTokenArrays(new io.github.llmcodestyle.quality.SpringBootMainVisibilityCheck());
        verifyTokenArrays(new io.github.llmcodestyle.quality.TestClassNamingCheck());
        verifyTokenArrays(new io.github.llmcodestyle.quality.TestMethodNameCheck());
        verifyTokenArrays(new io.github.llmcodestyle.quality.TestOnlyDelegateCheck());
        verifyTokenArrays(new io.github.llmcodestyle.quality.TopLevelEnumInEnumsPackageCheck());
        verifyTokenArrays(new io.github.llmcodestyle.quality.TopLevelRecordInPojosPackageCheck());
        verifyTokenArrays(new io.github.llmcodestyle.quality.UnrelatedNestedClassCheck());
        verifyTokenArrays(new io.github.llmcodestyle.quality.UnrelatedNestedEnumCheck());
        verifyTokenArrays(new io.github.llmcodestyle.quality.UnrelatedNestedInterfaceCheck());
        verifyTokenArrays(new io.github.llmcodestyle.quality.UnrelatedNestedRecordCheck());
        verifyTokenArrays(new io.github.llmcodestyle.quality.UnusedPrivateMembersCheck());
        verifyTokenArrays(new io.github.llmcodestyle.quality.UtilClassInUtilsPackageCheck());
        verifyTokenArrays(new io.github.llmcodestyle.quality.UtilClassNamingCheck());
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
