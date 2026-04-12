package com.example;

// Tests handleBlockCommentBody L50 mutation in BlankLineAfterCommentCheck
// L50: `stripped.substring(stripped.indexOf("*/") + 2).strip().isEmpty()`
// When negated: a block comment ending with code after */ would be treated as
// a comment-only line, marking lastCommentLine. Then a blank line before code
// would trigger a false positive.
@SuppressWarnings("unused")
public class BlankLineAfterCommentBlockEndCode {
    /* block comment end with code */ int inlineVar = 1;

    int normalVar = 2;
}
