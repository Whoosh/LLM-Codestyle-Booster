package com.example;

// Tests extractMethodName and countStatements mutations in TestOnlyDelegateCheck
@SuppressWarnings("unused")
public class TestOnlyDelegateCountStmts {

    // Non-private method that delegates to a private method via unqualified call (exercises extractMethodName L113)
    String doWork() {
        return internalWork();
    }

    // Non-private void delegate
    void doAction() {
        internalAction();
    }

    // Private method (delegate target)
    private String internalWork() {
        return "work";
    }

    // Private method (delegate target)
    private void internalAction() {
        System.out.println("action");
    }
}
