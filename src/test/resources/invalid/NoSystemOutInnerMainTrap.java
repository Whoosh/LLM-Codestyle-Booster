package com.example.service;

/** Inner class named *Main must NOT exempt the outer class. */
public class NoSystemOutInnerMainTrap {

    private static class InnerMain {

        static void run() {
        }
    }

    public void afterInnerClass() {
        System.out.println("must still fire after inner Main-named class");
    }
}
