package com.example;

// Tests findCommentStart char literal tracking
// If inChar toggling is broken, the parser might think // is a comment start
// inside a char literal context, or miss comments after char literals.
public class NoSuppressionInCharQuote {
    // The char literal '/' followed by another '/' should NOT be detected as //
    // because both slashes are inside separate char literals
    char a = '/';
    char b = '/';
    // The string "NOPMD" should not trigger because it's in a string, not a comment
    String s = "NOPMD";
}
