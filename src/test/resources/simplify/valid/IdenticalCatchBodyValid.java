package com.example;
public class IdenticalCatchBodyValid {
    void process(String input) {
        try {
            Integer.parseInt(input);
        } catch (NumberFormatException e) {
            throw new RuntimeException("number error", e);
        } catch (IllegalArgumentException e) {
            System.out.println("arg error");
        }
    }
}
