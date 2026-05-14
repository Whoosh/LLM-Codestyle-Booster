package com.example;

public class IdenticalCatchBodyIdenticalLiteralsInvalid {

    // Two catches with truly identical bodies returning the SAME double literal.
    // Must still be flagged after the fingerprint fix.
    double sameDoubleLiteral(String input) {
        try {
            return Double.parseDouble(input);
        } catch (NumberFormatException e) {
            return 1.5;
        } catch (NullPointerException e) {
            return 1.5;
        }
    }

    // Two catches with truly identical bodies returning the SAME boolean literal.
    boolean sameBooleanLiteral(String input) {
        try {
            return Boolean.parseBoolean(input);
        } catch (IllegalArgumentException e) {
            return true;
        } catch (NullPointerException e) {
            return true;
        }
    }
}
