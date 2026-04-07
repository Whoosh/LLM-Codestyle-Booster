package com.example;

import java.util.regex.Pattern;

public class InlineRegexValid {

    // Case 1: Pattern as static final — correct
    private static final Pattern DIGITS = Pattern.compile("\\d+");

    // Case 2: replaceAll with constant reference — correct
    private static final String REPLACEMENT = "_";
    void replaceWithConstant(String text) {
        String result = DIGITS.matcher(text).replaceAll(REPLACEMENT);
    }

    // Case 3: String.replace (not replaceAll) — not regex
    void replaceNonRegex(String text) {
        String result = text.replace("\\", "\\\\");
    }

    // Case 4: Regular method call, not regex
    void regularMethod() {
        String result = "hello".toUpperCase();
    }

    // Case 5: replaceAll with single-char regex — too simple to extract
    void replaceAllSingleChar(String text) {
        String result = text.replaceAll(" ", "_");
    }

    // Case 6: split with single-char regex
    void splitSingleChar(String text) {
        String[] parts = text.split("1");
    }

    // Case 7: replaceAll with empty string
    void replaceAllEmpty(String text) {
        String result = text.replaceAll("", "x");
    }

    // Case 8: matches with single char
    void matchesSingleChar(String text) {
        boolean ok = text.matches(".");
    }
}
