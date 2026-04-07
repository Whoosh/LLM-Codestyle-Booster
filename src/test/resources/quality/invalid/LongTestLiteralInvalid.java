package test;

import org.junit.jupiter.api.Test;

public class LongTestLiteralInvalid {

    @Test
    void testWithLongStrings() {
        String result = process("This is a very long string that exceeds thirty characters easily");
        String other = validate("Another long string literal that should be in a resource file");
    }

    // Regression: long literal as non-last arg of assert must still fire (it is a VALUE, not a message)
    @Test
    void testAssertEqualsExpectedValueLiteral() {
        String actual = process("short");
        org.junit.jupiter.api.Assertions.assertEquals(
            "This is a very long expected value that must be extracted to a resource",
            actual);
    }

    private String process(String s) {
        return s;
    }

    private String validate(String s) {
        return s;
    }
}
