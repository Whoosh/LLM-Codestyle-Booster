package com.example;

// Tests stringLiteralInitText (L89 SURVIVED, L93 NO_COVERAGE) in CommonsLang3StringConstantCheck
@SuppressWarnings("unused")
public class CommonsLang3MutKill {
    // static final String with empty string literal — should be flagged (EMPTY equivalent)
    static final String BLANK = "";

    // static final String with space literal — should be flagged (SPACE equivalent)
    static final String SEPARATOR = " ";

    // static final String with no ASSIGN — stringLiteralInitText returns null (L89)
    // Actually this won't compile without initializer for static final. Let's use newline instead.
    static final String NEWLINE = "\n";
}
