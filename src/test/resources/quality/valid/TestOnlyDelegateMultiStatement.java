package com.example;

// Tests countStatements L124: method with multiple statements should NOT be flagged
@SuppressWarnings("unused")
public class TestOnlyDelegateMultiStatement {

    // Non-private method with TWO statements — not a thin delegate
    String doWork() {
        String result = internalWork();
        return result;
    }

    private String internalWork() {
        return "work";
    }
}
