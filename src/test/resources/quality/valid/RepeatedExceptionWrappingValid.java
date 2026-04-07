package com.example;

import java.io.IOException;

public class RepeatedExceptionWrappingValid {

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
            throw new IllegalStateException("state error", e);
        }
    }

    private void loadFile(String name) throws IOException {}
}
