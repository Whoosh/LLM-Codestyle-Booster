package com.example.service;

/** Test class — System.out/err is allowed. */
public class OrderServiceTest {

    void shouldProcessOrder() {
        System.out.println("debug output in test");
        System.err.println("error output in test");
    }
}
