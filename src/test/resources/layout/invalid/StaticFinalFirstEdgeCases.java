package test;

public class StaticFinalFirstEdgeCases {

    // Static-only field (not final) does NOT count as "instance field or ctor"
    static String staticNonFinal = "a";

    // Instance field — triggers "seen instance" barrier
    private int instanceField = 0;

    // Constructor — also triggers "seen instance" barrier
    StaticFinalFirstEdgeCases() { }

    // This static final after a constructor should be flagged
    static final String AFTER_CTOR = "flagged";

    // Another instance field
    private String name = "x";

    // This static final after instance+ctor should be flagged
    static final int LATE_CONST = 99;

    // Nested class — check continues inside
    static class Nested {
        private int innerInstance = 5;
        static final String INNER_AFTER = "also flagged";
    }
}
