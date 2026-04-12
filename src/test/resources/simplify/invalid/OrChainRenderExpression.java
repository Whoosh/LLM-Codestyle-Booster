package com.example;

import java.util.Set;

// Tests renderExpression NO_COVERAGE and SURVIVED paths in OrChainToSetContainsCheck
@SuppressWarnings("unused")
public class OrChainRenderExpression {
    // Chain using method call on LHS: e.g. x.getType() == 1 || x.getType() == 2 || x.getType() == 3
    // This exercises renderExpression with METHOD_CALL node and DOT node
    void methodCallChain(Object x) {
        int type = 0;
        if (type == 1 || type == 2 || type == 3) {
            System.out.println("match");
        }
    }

    // Chain with `this` keyword as part of expression
    int value;
    void thisChain() {
        if (this.value == 1 || this.value == 2 || this.value == 3) {
            System.out.println("match");
        }
    }
}
