package test;

import org.junit.jupiter.api.Test;

public class LongTestLiteralMutationKiller {

    // Field inside nested class OBJBLOCK — exempt (isFieldInitializer should return true)
    static class Inner {
        private static final String NESTED_FIELD = "This is a long field initializer inside a nested class that should be exempt";
    }

    // Long string inside a test method, nested in a local variable inside a lambda
    // This exercises isAssertionMessage: the LAMBDA parent type should stop traversal
    @Test
    void testLambdaWithLongLiteral() {
        Runnable r = () -> {
            String x = "This is a very long string inside a lambda in a test method body that should be flagged";
        };
    }

    // Long string inside assertion that is NOT the last arg (isAssertionMessage false)
    @Test
    void testAssertEqualsFirstArgFlagged() {
        org.junit.jupiter.api.Assertions.assertEquals(
            "This very long expected string is the first argument and should be flagged by the check",
            "actual");
    }

    // Long string that IS the assertion message (last arg) — exempt
    @Test
    void testAssertEqualsLastArgExempt() {
        org.junit.jupiter.api.Assertions.assertEquals("a", "a",
            "This is a very long assertion message that should be exempt from the literal check");
    }

    // Long string in @Test method body directly (not field, not assertion message)
    @Test
    void testDirectBodyLiteral() {
        String val = "Another very long string literal that is directly in the test method body and flagged";
    }

    // Long string inside a VARIABLE_DEF that is inside SLIST (not OBJBLOCK)
    // isFieldInitializer should return false because parent VARIABLE_DEF's grandparent is SLIST not OBJBLOCK
    @Test
    void testLocalVarNotFieldInitializer() {
        String local = "This is a long local variable in a test body, grandparent is SLIST not OBJBLOCK so not exempt";
    }
}
