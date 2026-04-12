package com.example;

// Test edge cases for findCommentStart: char literals with quotes and escapes
public class NoSuppressionFindCommentEdge {
    // char literal with single-quote escape, then comment: exercises inChar toggling
    char sq = '\''; // NOPMD
    // string with escaped double-quote, then comment: exercises inString toggling
    String dq = "he said \"hello\""; // CHECKSTYLE:OFF
    // char literal with backslash, followed by a comment
    char bs = '\\'; // SUPPRESSFBWARNINGS
}
