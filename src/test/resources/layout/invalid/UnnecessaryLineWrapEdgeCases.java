package com.example;

public class UnnecessaryLineWrapEdgeCases {

    // Case 1: constructor that fits on one line (tests CTOR_DEF path via computeFirstLine)
    UnnecessaryLineWrapEdgeCases(
            String arg) {
    }

    // Case 2: abstract method (no SLIST, uses SEMI) that fits
    interface Iface {
        void doWork(
                String arg);
    }

    // Case 3: return that fits with closing bracket on next line
    String returnWithBracket() {
        return ("hello"
                + "!");
    }

    // Case 4: enum def with extends-like that fits
    @Deprecated
    static class AnnotatedClass
            extends Thread {
    }

    // Case 5: try-with-resources where each resource wraps but fits on one line
    void tryResourceWrap() throws Exception {
        try (AutoCloseable a
                     = open()) {
            // body
        }
    }

    private AutoCloseable open() {
        return null;
    }
}
