package com.example;

public class UnnecessaryLineWrapNeedsSpace {

    // This tests needsSpace with closing bracket ) on continuation line
    String testClosingParen() {
        return compute(
                "arg");
    }

    // This tests needsSpace with opening bracket ( as last char
    void testOpeningParen() {
        run(
                42);
    }

    // This tests needsSpace where last char is space already
    void testTrailingSpace() {
        String x =
                "value";
    }

    private String compute(String s) { return s; }
    private void run(int n) { }
}
