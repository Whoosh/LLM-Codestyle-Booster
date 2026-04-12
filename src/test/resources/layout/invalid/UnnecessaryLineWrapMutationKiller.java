package com.example;

import java.io.Closeable;

public abstract class UnnecessaryLineWrapMutationKiller {

    // Case 1: RESOURCE where tryHeaderFitsOnOneLine => false because try header is too long
    // This forces the RESOURCE to NOT be skipped, exercising the `type == RESOURCE && tryHeaderFitsOnOneLine(ast)` false path
    void tryWithLongResourceThatDoesNotFit() throws Exception {
        try (Closeable aVeryLongVariableNameForTestingPurposesHere =
                     openSomething()) {
            use(aVeryLongVariableNameForTestingPurposesHere);
        }
    }

    // Case 2: compact constructor wrapping (COMPACT_CTOR_DEF with TYPE as first token path)
    record TestRecord(int x, int y) {
        TestRecord {
        }
    }

    // Case 3: Method def where TYPE is null but IDENT exists (computeFirstLine line 93-95)
    // Constructor has no TYPE token but has IDENT
    UnnecessaryLineWrapMutationKiller(
            String arg1) {
    }

    // Case 4: Abstract method with no SLIST and no SEMI (findSignatureLastLine fallback)
    // This should test findLastLine path
    interface InnerIface {
        void shortMethod(
                String arg);
    }

    // Case 5: Variable def that wraps and has empty continuation line (buildCombinedLine)
    void emptyLineInWrappedStatement() {
        String longVariable =

                "value";
    }

    // Case 6: needsSpace: test with last char being space
    void spaceAtEnd() {
        String x =
                "hello";
    }

    // Case 7: needsSpace: closing bracket at start of continuation
    String closingBracketTest() {
        return compute(
                "arg");
    }

    // Case 8: needsSpace: opening bracket at end of line
    void openingBracketTest() {
        run(
                42);
    }

    // Case 9: class def with annotations where all modifiers are annotations
    // forces firstNonAnnotationLine to fall through to IDENT
    @Deprecated
    @SuppressWarnings("unused")
    static class FullyAnnotatedClass
            extends Thread {
    }

    // Case 10: class def without any modifiers at all (firstNonAnnotationLine modifiers==null path)
    // In practice, inner classes always have MODIFIERS, but interfaces inside methods might not

    // Case 11: if that wraps
    void wrappedIf() {
        boolean a = true;
        if (a
                || !a) {
            // body
        }
    }

    // Case 12: record that wraps
    record WrappedRecord(
            String name, int age) {
    }

    private Closeable openSomething() { return null; }
    private void use(Closeable c) { }
    private String compute(String s) { return s; }
    private void run(int n) { }
}
