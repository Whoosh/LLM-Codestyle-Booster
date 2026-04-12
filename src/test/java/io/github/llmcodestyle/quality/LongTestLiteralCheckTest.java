package io.github.llmcodestyle.quality;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.github.llmcodestyle.utils.TestCheckSupportUtil.*;
import static org.junit.jupiter.api.Assertions.*;

class LongTestLiteralCheckTest {

    private static final int EXPECTED_VIOLATIONS = 3;
    private static final String SHORT_MAX = "10";
    private static final Map<String, String> NO_PROPS = Map.of();

    private static final int LINE_FIELD_INIT = 8;
    private static final int LINE_NESTED_FIELD = 9;
    private static final int LINE_BODY_LITERAL = 13;
    private static final int LINE_LAMBDA_BODY = 17;
    private static final int LINE_ASSERT_EXPECTED = 19;
    private static final int LINE_ASSERT_EXPECTED_ALT = 20;
    private static final int LINE_ASSERT_EQUALS_FIRST = 25;
    private static final int LINE_ASSERT_TRUE_MSG = 27;
    private static final int LINE_ASSERT_EQUALS_MSG = 33;
    private static final int LINE_FAIL_MSG = 34;
    private static final int LINE_DIRECT_BODY = 39;
    private static final int LINE_HELPER = 40;
    private static final int LINE_LOCAL_VAR = 46;
    private static final int EXPECTED_MUT_KILLER_VIOLATIONS = 4;

    @Test
    void invalidCasesProduceViolations() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(LongTestLiteralCheck.class, "quality/invalid/LongTestLiteralInvalid.java", NO_PROPS);
        assertEquals(EXPECTED_VIOLATIONS, violations.size(), "Expected 3 long test literal violations, got " + format(violations));
    }

    @Test
    void validCasesProduceNoViolations() throws Exception {
        assertTrue(runTreeWalkerCheck(LongTestLiteralCheck.class, "quality/valid/LongTestLiteralValid.java", NO_PROPS).isEmpty(), "Expected no violations");
    }

    @Test
    void setMaxLengthAffectsThreshold() throws Exception {
        new LongTestLiteralCheck().setMaxLength(Integer.parseInt(SHORT_MAX));
        assertFalse(
            runTreeWalkerCheck(LongTestLiteralCheck.class, "quality/invalid/LongTestLiteralInvalid.java", Map.of("maxLength", SHORT_MAX)).isEmpty(),
            "Smaller max length should produce violations");
    }

    @Test
    void edgeCaseValidFixtureProducesNoViolations() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(LongTestLiteralCheck.class, "quality/valid/LongTestLiteralEdgeCases.java", NO_PROPS);
        assertTrue(violations.isEmpty(), "Edge case valid fixture should produce no violations: " + format(violations));
    }

    @Test
    void fieldConstantsAreExempt() throws Exception {
        assertTrue(runTreeWalkerCheck(LongTestLiteralCheck.class, "quality/valid/LongTestLiteralValid.java", NO_PROPS).isEmpty(), "Field constants should be exempt");
    }

    @Test
    void assertionMessagesAreExempt() throws Exception {
        assertTrue(runTreeWalkerCheck(LongTestLiteralCheck.class, "quality/valid/LongTestLiteralValid.java", NO_PROPS).isEmpty(), "Assertion messages (last arg) should be exempt");
    }

    @Test
    void displayNameAnnotationIsExempt() throws Exception {
        assertTrue(
            runTreeWalkerCheck(LongTestLiteralCheck.class, "quality/valid/LongTestLiteralEdgeCases.java", NO_PROPS).isEmpty(),
            "DisplayName annotation values should be exempt");
    }

    @Test
    void violationMessageContainsLengthInfo() throws Exception {
        for (AuditEvent event : runTreeWalkerCheck(LongTestLiteralCheck.class, "quality/invalid/LongTestLiteralInvalid.java", NO_PROPS)) {
            String msg = event.getMessage();
            assertTrue(msg.contains("30") || msg.contains("character"), "Message should reference max length: " + msg);
        }
    }

    @Test
    void nonTestMethodLongLiteralsAreExempt() throws Exception {
        assertTrue(
            runTreeWalkerCheck(LongTestLiteralCheck.class, "quality/valid/LongTestLiteralEdgeCases.java", NO_PROPS).isEmpty(),
            "Long literals in non-test methods should be exempt");
    }

    @Test
    void fieldInitializerExemptAndBodyLiteralFlagged() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(LongTestLiteralCheck.class, "quality/invalid/LongTestLiteralFieldAndAssert.java", NO_PROPS);
        // Field constant should be exempt, helper non-test method exempt,
        // body literals in @Test should be flagged, assertion messages exempt
        // Line 13: long literal in test body = flagged
        // Line 19: long expected value in assertEquals = flagged
        assertTrue(violations.stream().anyMatch(v -> v.getLine() == LINE_BODY_LITERAL), "Long literal in test body should be flagged: " + format(violations));
        assertTrue(
            violations.stream().anyMatch(v -> v.getLine() == LINE_ASSERT_EXPECTED || v.getLine() == LINE_ASSERT_EXPECTED_ALT),
            "Long expected value in assertEquals should be flagged: " + format(violations));
        // Field on line 8 should NOT be flagged
        assertTrue(violations.stream().noneMatch(v -> v.getLine() == LINE_FIELD_INIT), "Field initializer should be exempt: " + format(violations));
        // Assertion messages should NOT be flagged (they are the last args)
        assertEquals(2, violations.size(), "Only body literal and assertEquals expected value should be flagged: " + format(violations));
    }

    @Test
    void assertionMessageLastArgIsExempt() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(LongTestLiteralCheck.class, "quality/invalid/LongTestLiteralFieldAndAssert.java", NO_PROPS);
        // assertTrue message on line 27 should be exempt
        assertTrue(violations.stream().noneMatch(v -> v.getLine() == LINE_ASSERT_TRUE_MSG), "assertTrue message should be exempt: " + format(violations));
        // fail message on line 34 should be exempt
        assertTrue(violations.stream().noneMatch(v -> v.getLine() == LINE_FAIL_MSG), "fail message should be exempt: " + format(violations));
    }

    @Test
    void helperMethodNotAnnotatedIsExempt() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(LongTestLiteralCheck.class, "quality/invalid/LongTestLiteralFieldAndAssert.java", NO_PROPS);
        // Line 40: helper method not annotated with @Test should be exempt
        assertTrue(violations.stream().noneMatch(v -> v.getLine() == LINE_HELPER), "Helper method without @Test should be exempt: " + format(violations));
    }

    @Test
    void mutationKillerFlagsBodyLiteralsNotFieldsOrMessages() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(LongTestLiteralCheck.class, "quality/invalid/LongTestLiteralMutationKiller.java", NO_PROPS);
        // Should flag: lambda body literal(17), assertEquals first arg(25), direct body(39), local var(46)
        assertEquals(EXPECTED_MUT_KILLER_VIOLATIONS, violations.size(), "Expected exactly 4 violations in mutation killer fixture: " + format(violations));
        // Lambda body literal (line 17)
        assertTrue(violations.stream().anyMatch(v -> v.getLine() == LINE_LAMBDA_BODY), "Lambda body literal should be flagged: " + format(violations));
        // assertEquals first arg (line 25)
        assertTrue(violations.stream().anyMatch(v -> v.getLine() == LINE_ASSERT_EQUALS_FIRST), "assertEquals first arg should be flagged: " + format(violations));
        // Direct body literal (line 39)
        assertTrue(violations.stream().anyMatch(v -> v.getLine() == LINE_DIRECT_BODY), "Direct body literal should be flagged: " + format(violations));
        // Local var (line 46) — grandparent is SLIST not OBJBLOCK
        assertTrue(violations.stream().anyMatch(v -> v.getLine() == LINE_LOCAL_VAR), "Local var in test body should be flagged: " + format(violations));
    }

    @Test
    void assertionMessageExemptInMutationKiller() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(LongTestLiteralCheck.class, "quality/invalid/LongTestLiteralMutationKiller.java", NO_PROPS);
        // assertEquals last arg message (line 33) should be exempt
        assertTrue(violations.stream().noneMatch(v -> v.getLine() == LINE_ASSERT_EQUALS_MSG), "assertEquals last arg message should be exempt: " + format(violations));
    }

    @Test
    void nestedFieldInsideInnerClassIsExempt() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(LongTestLiteralCheck.class, "quality/invalid/LongTestLiteralMutationKiller.java", NO_PROPS);
        // Nested class field (line 9) should be exempt — isFieldInitializer returns true
        assertTrue(violations.stream().noneMatch(v -> v.getLine() == LINE_NESTED_FIELD), "Nested class field should be exempt: " + format(violations));
    }

    @Test
    void isFieldInitializerWalksUpToMethodDef() throws Exception {
        // Tests that isFieldInitializer breaks at METHOD_DEF (line 79)
        // Local var inside test method has VARIABLE_DEF -> SLIST -> METHOD_DEF
        // If the break at METHOD_DEF were negated, it would incorrectly treat local vars as field initializers
        List<AuditEvent> violations = runTreeWalkerCheck(LongTestLiteralCheck.class, "quality/invalid/LongTestLiteralMutationKiller.java", NO_PROPS);
        assertTrue(violations.stream().anyMatch(v -> v.getLine() == LINE_DIRECT_BODY), "Local var in test method should be flagged, not treated as field: " + format(violations));
    }

    @Test
    void fieldInsideLocalClassInTestMethodIsExempt() throws Exception {
        // A field initializer inside a local class within a @Test method
        // isInsideTestMethod returns true, but isFieldInitializer MUST return true to exempt
        // This kills NEGATE_CONDITIONALS on lines 72, 75 (parent traversal and OBJBLOCK check)
        List<AuditEvent> violations = runTreeWalkerCheck(LongTestLiteralCheck.class, "quality/valid/LongTestLiteralFieldInTestMethod.java", NO_PROPS);
        assertTrue(violations.isEmpty(), "Field initializer inside local class in test method should be exempt: " + format(violations));
    }

    @Test
    void isAssertionMessageWhileLoopHandlesLambdaParent() throws Exception {
        // isAssertionMessage line 106: while (node != null && node.getType() != EXPR && != METHOD_DEF && != LAMBDA)
        // Lambda parent should stop the traversal
        List<AuditEvent> violations = runTreeWalkerCheck(LongTestLiteralCheck.class, "quality/invalid/LongTestLiteralMutationKiller.java", NO_PROPS);
        // Lambda body literal (line 17) is inside a lambda, which stops isAssertionMessage traversal
        // This means it's NOT treated as an assertion message and IS flagged
        assertTrue(
            violations.stream().anyMatch(v -> v.getLine() == LINE_LAMBDA_BODY),
            "Lambda body literal should be flagged (lambda stops assertion message traversal): " + format(violations));
    }

    @Test
    void isAssertionMessageExercisesAllPaths() throws Exception {
        // L106 SURVIVED/NO_COVERAGE: the while loop in isAssertionMessage checks for
        // EXPR, METHOD_DEF, and LAMBDA. We need to exercise assertion messages with:
        // 1. Standard assert message (assertEquals last arg) - exempt
        // 2. fail() message - exempt
        // 3. Long string NOT in assert - flagged
        // 4. String inside lambda (LAMBDA stops traversal) - flagged
        List<AuditEvent> violations = runTreeWalkerCheck(LongTestLiteralCheck.class, "quality/invalid/LongTestLiteralAssertMessage.java", NO_PROPS);
        // Non-message string and lambda string should be flagged
        // Assertion messages should be exempt
        assertTrue(violations.size() >= 2, "At least 2 non-assertion-message strings should be flagged: " + format(violations));
        // Verify that fail() message is NOT flagged (it's the last arg of fail())
        assertTrue(violations.stream().noneMatch(v -> v.getMessage().contains("failure message")), "fail() message should be exempt: " + format(violations));
    }
}
