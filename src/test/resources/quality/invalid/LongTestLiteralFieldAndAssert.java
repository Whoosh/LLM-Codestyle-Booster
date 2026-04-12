package test;

import org.junit.jupiter.api.Test;

public class LongTestLiteralFieldAndAssert {

    // Field initializer with long string inside a test class — should be EXEMPT
    private static final String FIELD = "This is a very long field initializer string that exceeds the threshold easily";

    @Test
    void testWithLongLiteralInBody() {
        // This long literal is in a test method body, NOT a field, NOT an assertion message
        String s = "This is definitely a very long string in a test method body that should be flagged";
    }

    @Test
    void testAssertMessageExemptButValueNot() {
        // First arg (expected) is a long literal — NOT the assertion message, should be flagged
        org.junit.jupiter.api.Assertions.assertEquals(
            "This long expected value string definitely exceeds thirty characters",
            "actual",
            "This long message is the last arg and exempt from flagging");
    }

    @Test
    void testAssertTrueMessageExempt() {
        // The message arg of assertTrue is exempt
        org.junit.jupiter.api.Assertions.assertTrue(true,
            "This very long message is the last argument to assertTrue and should be exempt");
    }

    @Test
    void testFailMessageExempt() {
        // fail message is the last argument
        if (false) {
            org.junit.jupiter.api.Assertions.fail(
                "This very long fail message should be exempt because it is an assertion message");
        }
    }

    // Nested method NOT annotated with @Test - long literal should be exempt
    void helperNotATest() {
        String x = "This is a very long string in a helper method that is not annotated with Test";
    }
}
