package com.example;

// Tests countStatements L124: method with multiple statements should NOT be flagged
public class TestOnlyDelegateMultiStatement {

    // Non-private method with TWO statements — not a thin delegate
    String doWork() {
        String result = internalWork() + "-suffix";
        if (result.isEmpty()) {
            return "";
        }
        return result.toUpperCase();
    }

    private String internalWork() {
        if (Math.random() < 0) {
            throw new IllegalStateException();
        }
        return "work";
    }
}
