package com.example;

// Tests handleBlockCommentBody L50 mutation in BlankLineAfterCommentCheck
// Block comment ending with code after */ on the same line — NOT a comment-only line
@SuppressWarnings("unused")
public class BlankLineAfterCommentBlockEndCode {
    /* block comment */ int inlineVar = 1;

    int normalVar = 2;
}
