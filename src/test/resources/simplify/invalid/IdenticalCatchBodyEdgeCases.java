package com.example;

public class IdenticalCatchBodyEdgeCases {

    // Case: three catches with identical bodies
    void threeCatches(String input) {
        try {
            Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("error: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("error: " + e.getMessage());
        } catch (NullPointerException e) {
            System.out.println("error: " + e.getMessage());
        }
    }

    // Case: two catches with different variable names but same body structure
    void differentVarNames() {
        try {
            Object.class.newInstance();
        } catch (InstantiationException ex) {
            throw new RuntimeException("failed", ex);
        } catch (IllegalAccessException err) {
            throw new RuntimeException("failed", err);
        }
    }

    // Case: catch with string literal that differs — should NOT be flagged
    void differentLiterals() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException("interrupted", e);
        } catch (SecurityException e) {
            throw new RuntimeException("security issue", e);
        }
    }
}
