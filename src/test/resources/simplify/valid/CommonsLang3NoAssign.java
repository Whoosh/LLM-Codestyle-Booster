package com.example;

// Tests stringLiteralInitText returning null for non-string-literal initializers (NO_COVERAGE on L93)
public class CommonsLang3NoAssign {
    // static final String with non-literal initializer — should NOT be flagged
    static final String COMPUTED = String.valueOf(42);

    // static final String with concatenation expression that isn't collapsible — should NOT be flagged
    static final String CONCAT = "a" + COMPUTED;

    // static final non-String — should NOT be flagged
    static final int COUNT = 0;

    // non-static-final String with literal — should NOT be flagged
    String mutable = "";
}
