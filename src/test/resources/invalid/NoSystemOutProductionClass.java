package com.example.service;

/** Regular production class — System.out/err is forbidden. */
public class NoSystemOutProductionClass {

    public void doWork() {
        System.out.println("starting work");
        System.err.println("error occurred");
        System.out.print("no newline");
    }
}
