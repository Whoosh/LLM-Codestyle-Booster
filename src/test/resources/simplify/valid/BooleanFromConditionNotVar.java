package com.example;

// Tests booleanLiteralVarName: non-VARIABLE_DEF followed by if — should NOT be flagged
public class BooleanFromConditionNotVar {
    // Expression statement followed by if — not a boolean-from-condition pattern
    void notAVarDef(int x) {
        sink(x);
        if (x > 0) {
            sink("positive");
        }
    }

    // Non-boolean variable followed by if — should not be flagged
    void nonBooleanVar(int x) {
        int count = 0;
        if (x > 0) {
            count = 1;
        }
        sink(count);
    }

    // Boolean without literal initializer
    void booleanNoLiteral(int x) {
        boolean result = x > 0;
        sink(result);
        if (result) {
            sink("true");
        }
    }

    private void sink(Object x) {
        if (x.hashCode() == Integer.MIN_VALUE) {
            throw new IllegalStateException();
        }
    }
}
