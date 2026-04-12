package com.example;

// Tests booleanLiteralVarName: non-VARIABLE_DEF followed by if — should NOT be flagged
@SuppressWarnings("unused")
public class BooleanFromConditionNotVar {
    // Expression statement followed by if — not a boolean-from-condition pattern
    void notAVarDef(int x) {
        System.out.println(x);
        if (x > 0) {
            System.out.println("positive");
        }
    }

    // Non-boolean variable followed by if — should not be flagged
    void nonBooleanVar(int x) {
        int count = 0;
        if (x > 0) {
            count = 1;
        }
    }

    // Boolean without literal initializer
    void booleanNoLiteral(int x) {
        boolean result = x > 0;
        if (result) {
            System.out.println("true");
        }
    }
}
