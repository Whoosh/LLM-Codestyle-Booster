package com.example.service;

/** Regular production class with no System.out/err — passes. */
public class OrderService {

    public void doWork() {
        String result = process();
    }

    private String process() {
        return "ok";
    }
}
