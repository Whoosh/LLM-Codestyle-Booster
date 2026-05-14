package com.example;

public class IdenticalCatchBodyDifferentLiteralsValid {

    // Two catches differing only in a double literal (1.5 vs 2.5).
    // After the fix these must NOT be treated as identical.
    double differentDoubleLiterals(String input) {
        try {
            return Double.parseDouble(input);
        } catch (NumberFormatException e) {
            return 1.5;
        } catch (NullPointerException e) {
            return 2.5;
        }
    }

    // Two catches differing only in a float literal (1.5f vs 2.5f).
    float differentFloatLiterals(String input) {
        try {
            return Float.parseFloat(input);
        } catch (NumberFormatException e) {
            return 1.5f;
        } catch (NullPointerException e) {
            return 2.5f;
        }
    }

    // Two catches differing only in a boolean literal (true vs false).
    boolean differentBooleanLiterals(String input) {
        try {
            return Boolean.parseBoolean(input);
        } catch (IllegalArgumentException e) {
            return true;
        } catch (NullPointerException e) {
            return false;
        }
    }

    // Two catches differing only in a char literal ('a' vs 'b').
    char differentCharLiterals(String input) {
        try {
            return input.charAt(0);
        } catch (IndexOutOfBoundsException e) {
            return 'a';
        } catch (NullPointerException e) {
            return 'b';
        }
    }

    // Two catches differing only in null vs a string — distinct LITERAL_NULL fingerprint.
    @jakarta.annotation.Nullable
    String differentNullLiterals(String input) {
        try {
            return input.trim();
        } catch (NullPointerException e) {
            return null;
        } catch (IllegalStateException e) {
            return "fallback";
        }
    }
}
