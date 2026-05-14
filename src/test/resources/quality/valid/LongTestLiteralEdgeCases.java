package test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class LongTestLiteralEdgeCasesTest {

    // Field initializers with long strings — always exempt
    private static final String FIELD_LONG = "This is a very long field initializer string that should always be exempt";

    // Non-test method with long string — not inside @Test, should be exempt
    void helperMethod() {
        String s = "This very long string is inside a non-test method and should be exempt from checking";
    }

    @Test
    @DisplayName("A long display name that should be exempt from the check because it is inside DisplayName annotation")
    void testWithDisplayName() {
        String x = "short";
        org.junit.jupiter.api.Assertions.assertNotNull(x);
        org.junit.jupiter.api.Assertions.assertEquals("short", x);
    }

    // Assertion message (last arg) — exempt
    @Test
    void testAssertMessageExempt() {
        org.junit.jupiter.api.Assertions.assertTrue(true, "This very long assertion message is the last argument and should be exempt");
    }

    @ParameterizedTest
    @MethodSource("data")
    void parameterizedTestWithShortLiteral(String input) {
        org.junit.jupiter.api.Assertions.assertNotNull(input);
    }

    static java.util.stream.Stream<String> data() {
        return java.util.stream.Stream.of("a", "b");
    }

    // verify* methods as assertion messages — exempt
    @Test
    void verifyCallWithLongMessage() {
        verifyCondition(true, "This very long verification message explains what was checked in detail here");
    }

    private void verifyCondition(boolean cond, String msg) {
        if (!cond) {
            throw new AssertionError(msg);
        }
    }
}
