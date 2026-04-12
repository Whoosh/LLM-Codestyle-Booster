package com.example;

public class ConditionalReturnToTernaryMutKiller {

    // Case 1: if-else with nested ternary in return — should NOT be flagged
    // containsType must recurse to find QUESTION nested inside the expression
    // If containsType's loop is broken (child != null → child == null), the ternary won't be found
    String nestedTernary(boolean a, boolean b) {
        if (a) {
            return b ? "yes" : "no";
        } else {
            return "default";
        }
    }

    // Case 2: if-else with deeply nested expression (depth > 3) — should NOT be flagged
    // depth must recurse through children to compute actual depth
    // If depth's loop is broken, depth always returns 1, which <= 3, so this would be flagged
    int deepExpression(boolean flag, int a, int b, int c) {
        if (flag) {
            return (a + (b * (c + 1)));
        } else {
            return 0;
        }
    }
}
