package com.example;

// Tests extractMethodName with no DOT (NO_COVERAGE on L73) — unqualified regex method call
@SuppressWarnings("unused")
public class InlineRegexExtractMethodName {
    // Unqualified method call: matches("longRegexPattern")
    // This exercises the `dot == null` path in extractMethodName (L73):
    // `return ident != null ? ident.getText() : null;`
    void unqualifiedRegexCall(String input) {
        String result = input.matches("\\d{3}-\\d{4}");
    }
}
