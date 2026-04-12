package com.example;

// Tests findArrayInit with LITERAL_NEW (NO_COVERAGE on L197) and scanForCollapsibleRuns (L250)
@SuppressWarnings("unused")
public class CollapsibleConstantArrayInit {
    // LITERAL_NEW -> ARRAY_INIT path in findArrayInit
    static final String[] ITEMS = new String[] {"a" + "b", "c"};

    // scanForCollapsibleRuns: PLUS in method body that is NOT nested in another PLUS
    void methodWithConcats() {
        String result = "hello" + " " + "world" + compute();
    }

    private String compute() { return "x"; }
}
