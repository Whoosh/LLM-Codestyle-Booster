package com.example;

import java.io.IOException;

public class RepeatedExceptionWrappingInvalid {

    void methodA() {
        try {
            loadFile("a");
        } catch (IOException e) {
            throw new RuntimeException("IO failed", e);
        }
    }

    void methodB() {
        try {
            loadFile("b");
        } catch (IOException e) {
            throw new RuntimeException("IO failed", e);
        }
    }

    void methodC() {
        try {
            loadFile("c");
        } catch (IOException e) {
            throw new RuntimeException("IO failed", e);
        }
    }

    private void loadFile(String name) throws IOException {}
}
