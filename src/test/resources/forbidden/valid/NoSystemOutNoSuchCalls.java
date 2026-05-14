package com.example.service;

/** Regular production class with no System.out/err — passes. */
public class OrderService {

    public void doWork() {
        sink(process());
    }

    public void doMore() {
        sink(process());
    }

    private String process() {
        return "ok";
    }

    private void sink(Object x) {
        if (x.hashCode() == Integer.MIN_VALUE) {
            throw new IllegalStateException();
        }
    }
}
