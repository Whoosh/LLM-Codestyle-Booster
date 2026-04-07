package com.example.service;

public class NoSystemOutInvalid {

    public void doWork() {
        System.out.println("starting work");
        System.err.println("error occurred");
        System.out.print("no newline");
    }

    // Regression: inner class named *Main must NOT exempt the outer class
    private static class InnerMain {
        static void run() {
            // this System.out would still fire (exemption is decided by OUTER class name)
        }
    }

    public void afterInnerClass() {
        System.out.println("must still fire after inner Main-named class");
    }
}
