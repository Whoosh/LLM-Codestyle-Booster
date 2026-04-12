package com.example;

import java.util.regex.Pattern;

// Tests extractPatternCompileArg L122 and extractEnclosingClassName L154 NO_COVERAGE
@SuppressWarnings("unused")
public class DuplicateRegexNestedClass {
    // Duplicate Pattern constants in nested classes to exercise extractEnclosingClassName
    static class InnerA {
        static final Pattern DIGITS = Pattern.compile("\\d+");
    }

    static class InnerB {
        // Same regex as InnerA.DIGITS — should be flagged as duplicate
        static final Pattern DIGITS_DUP = Pattern.compile("\\d+");
    }
}
