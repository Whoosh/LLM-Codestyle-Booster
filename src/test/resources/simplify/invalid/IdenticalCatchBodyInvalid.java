package com.example;
public class IdenticalCatchBodyInvalid {
    void process(String input) {
        try {
            Integer.parseInt(input);
        } catch (NumberFormatException e) {
            throw new RuntimeException("parse error", e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("parse error", e);
        }
    }
}
