package com.example;

// Tests extractMethodName NO_COVERAGE on L73: unqualified regex method call (no DOT)
@SuppressWarnings("unused")
public class InlineRegexUnqualified {
    // Unqualified call to a method named 'split' — exercises the ident != null path
    void callUnqualifiedSplit() {
        String result = split("hello-world-test");
    }

    // Local method named split that accepts a regex
    private String split(String input) {
        return input.split("\\d+-\\w+")[0];
    }
}
