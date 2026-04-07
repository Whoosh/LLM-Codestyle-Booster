package com.example;

import java.util.regex.Pattern;

public class InlineRegexInvalid {

    // Case 1: Pattern.compile inside method
    void compileInMethod() {
        Pattern p = Pattern.compile("\\d+");
    }

    // Case 2: String.replaceAll with regex literal
    void replaceAllInMethod(String text) {
        String cleaned = text.replaceAll("\\$[^$]*\\$", " ");
    }

    // Case 3: String.split with regex literal
    void splitInMethod(String text) {
        String[] parts = text.split("[^\\p{L}]+");
    }

    // Case 4: String.matches with regex literal
    void matchesInMethod(String text) {
        boolean ok = text.matches("\\d{4}-\\d{2}-\\d{2}");
    }

    // Case 5: String.replaceFirst with regex literal
    void replaceFirstInMethod(String text) {
        String result = text.replaceFirst("^\\s+", "");
    }
}
