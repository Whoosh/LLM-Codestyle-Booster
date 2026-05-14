package com.example;

import java.io.IOException;

public class RepeatedExceptionWrappingNestedInterfaceValid {

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

    private void loadFile(String name) throws IOException {}

    private interface Spi {
        default void foo() {
            try {
                doSpi();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        void doSpi() throws IOException;
    }
}
