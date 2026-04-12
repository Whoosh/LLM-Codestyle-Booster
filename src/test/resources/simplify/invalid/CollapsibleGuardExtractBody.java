package com.example;

// Tests extractIfBody with LITERAL_THROW body type (NO_COVERAGE on L100)
@SuppressWarnings("unused")
public class CollapsibleGuardExtractBody {
    // Guard that uses throw instead of return — extractIfBody must handle LITERAL_THROW
    void guardWithThrow(boolean a, boolean b) {
        if (a) throw new RuntimeException();
        if (b) {
            System.out.println("b");
        }
    }
}
