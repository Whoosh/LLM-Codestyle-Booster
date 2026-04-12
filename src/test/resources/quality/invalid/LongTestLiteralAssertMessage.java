package com.example;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

// Tests isAssertionMessage path (L106 SURVIVED/NO_COVERAGE) in LongTestLiteralCheck
// The assertion message (last arg) should be exempted, while non-message long strings should be flagged.
@SuppressWarnings("unused")
class LongTestLiteralAssertMessage {

    @Test
    void testWithLongAssertionMessage() {
        // Long string as assertion message (last arg) - should NOT be flagged
        assertEquals("expected", "expected", "This is a very long assertion message that describes the expected behavior of this test method");

        // Long string as non-message argument - SHOULD be flagged
        assertEquals("This is a very long string that should definitely be flagged by the check because it exceeds thirty chars", "actual");
    }

    @Test
    void testWithFailMessage() {
        fail("This is a long failure message that should be exempt because it is the message of a fail call");
    }

    @Test
    void testLongStringNotInAssert() {
        // Long string used outside of assertion — should be flagged
        String x = process("This is a long string value that absolutely exceeds the maximum length threshold");
    }

    @Test
    void testInsideLambda() {
        // Long string inside a lambda expression should be flagged (isAssertionMessage walks up and hits LAMBDA)
        Runnable r = () -> process("This is a very long string inside a lambda body that should be flagged");
    }

    private String process(String s) { return s; }
}
