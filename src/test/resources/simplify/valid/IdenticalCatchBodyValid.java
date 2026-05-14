package com.example;
public class IdenticalCatchBodyValid {
    void process(String input) {
        try {
            Integer.parseInt(input);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("number error", e);
        } catch (IllegalStateException e) {
            sink("state error");
        }
    }
    private void sink(Object x) {
        if (x.hashCode() == Integer.MIN_VALUE) {
            throw new IllegalStateException();
        }
    }
}
