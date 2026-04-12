package com.example.service;

/** Production class using printf and format — should be flagged. */
public class NoSystemOutPrintfFormatCall {

    public void doWork() {
        System.out.printf("formatted %s output%n", "test");
        System.err.format("error %d occurred%n", 42);
    }
}
