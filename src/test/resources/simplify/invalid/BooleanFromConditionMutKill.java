package com.example;

// Tests booleanLiteralVarName returns (L64, L68) in BooleanFromConditionCheck
// L64: `if (stmt.getType() != VARIABLE_DEF) return null;` — negating makes VARIABLE_DEF skip
// L68: `if ... || literalKindOfInit == 0) return null;` — negating allows non-boolean types through
@SuppressWarnings("unused")
public class BooleanFromConditionMutKill {
    // Standard boolean-from-condition that should be flagged
    void standardCase(int x) {
        boolean positive = false;
        if (x > 0) {
            positive = true;
        }
    }

    // Mirror case: true -> false
    void mirrorCase(int x) {
        boolean negative = true;
        if (x < 0) {
            negative = false;
        }
    }
}
