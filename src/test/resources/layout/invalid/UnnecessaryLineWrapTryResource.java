package com.example;

import java.io.Closeable;

public class UnnecessaryLineWrapTryResource {

    // Resource wrapping that fits on one line, but the overall try does not
    // This exercises tryHeaderFitsOnOneLine for a RESOURCE that has a parent
    // RESOURCE_SPECIFICATION whose parent is LITERAL_TRY spanning multiple lines.
    void tryWithShortResource() throws Exception {
        try (Closeable c =
                     open()) {
            use(c);
        }
    }

    // Multi-line try where try header itself fits on one line
    // The RESOURCE should be skipped because tryHeaderFitsOnOneLine returns true
    void shortTryHeader() throws Exception {
        try (Closeable c = open()) {
            use(c);
        }
    }

    // Abstract method on interface (SEMI instead of SLIST for findSignatureLastLine)
    interface Processor {
        void process(String a,
                     String b);
    }

    // Class with annotation on separate line, but definition fits on one line
    // Tests firstNonAnnotationLine with modifiers that have ANNOTATION first
    @Deprecated
    static class ShortAnnotatedClass
            extends Thread {
    }

    // Method with modifiers but no annotation - firstNonAnnotationLine should return first mod
    public static void
            shortMethod() {
    }

    // Empty continuation line in buildCombinedLine
    void emptyLineContinuation() {
        String x =

                "value";
    }

    private Closeable open() { return null; }
    private void use(Closeable c) { }
}
