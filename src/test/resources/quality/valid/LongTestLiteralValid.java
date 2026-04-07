package test;

import org.junit.jupiter.api.Test;

public class LongTestLiteralValid {

    private static final String LONG_CONST = "This long string is a field constant and not flagged";

    @Test
    void testWithShortStrings() {
        // Short strings are fine
        String result = process("hello");
        String other = validate("world");
    }

    @Test
    void testAssertionMessages() {
        // Short strings do not violate
        String result = process("short");
        validate("ok");
    }

    @Test
    void testLongMessageAsLastArg() {
        // Long literal AS LAST ARG of assert is a message — exempt
        String actual = process("x");
        org.junit.jupiter.api.Assertions.assertEquals("x", actual,
            "This long assertion message explains exactly why the equality failed");
    }

    @Test
    void testFailWithLongMessage() {
        // fail(msg) — msg is the only arg, exempt
        org.junit.jupiter.api.Assertions.fail(
            "This is a very long failure message that explains what went wrong");
    }

    private String process(String s) {
        return s;
    }

    private String validate(String s) {
        return s;
    }
}
