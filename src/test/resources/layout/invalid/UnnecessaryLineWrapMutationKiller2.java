package com.example;

import java.io.Closeable;

@SuppressWarnings("unused")
public abstract class UnnecessaryLineWrapMutationKiller2 {

    // --- shouldSkip: type == RESOURCE && tryHeaderFitsOnOneLine ---
    // A RESOURCE that wraps where the try header does NOT fit on one line.
    // If `type == RESOURCE` is negated, non-RESOURCE types would be checked against tryHeaderFitsOnOneLine.
    void resourceThatDoesNotFitOnOneLine() throws Exception {
        try (Closeable veryLongResourceVariableNameThatMakesItNotFitOnLine =
                     open()) {
            use(veryLongResourceVariableNameThatMakesItNotFitOnLine);
        }
    }

    // --- computeFirstLine: typeToken != null path for VARIABLE_DEF ---
    // Variable def with TYPE that wraps — the first line should be the TYPE's line
    void wrappedVariableDef() {
        String veryLongVariableNameHere =
                "value";
    }

    // --- firstNonAnnotationLine: modifiers != null && non-annotation modifier ---
    // Class with non-annotation modifier first (e.g., "public")
    // If `modifiers != null` negated, would skip to ident, changing start line
    public static class ModifiedClass
            extends Thread {
    }

    // --- firstNonAnnotationLine: ident != null return ---
    // Class with all-annotation modifiers, falls through to IDENT
    @Deprecated
    class AllAnnotationClass
            extends Thread {
    }

    // --- buildCombinedLine: needsSpace with space-at-end, closing-bracket, opening-bracket ---
    // Tests needsSpace: sb.isEmpty() path
    // Continuation line right after a line that produces empty stripped
    void needsSpaceEmptySb() {
        String x =
                "hello";
    }

    // Tests needsSpace: CLOSING_BRACKETS path - continuation starts with )
    String closingBracket() {
        return compute(
                "arg");
    }

    // Tests needsSpace: OPENING_BRACKETS path - line ends with (
    void openingBracket() {
        run(
                42);
    }

    // Tests needsSpace: last != ' ' path
    void normalSpace() {
        String y = compute(
                "test");
    }

    // --- findSignatureLastLine: semi != null for abstract method ---
    abstract void abstractMethod(
            String param);

    // --- tryHeaderFitsOnOneLine: tryAst == null (NO_COVERAGE) ---
    // (Not directly testable through normal means - requires RESOURCE not under LITERAL_TRY)

    // --- record wrapping ---
    record WrappedRecord2(
            String name) {
    }

    // --- if wrapping ---
    void wrappedIf() {
        boolean flag = true;
        if (flag
                || !flag) {
            // body
        }
    }

    private Closeable open() { return null; }
    private void use(Closeable c) { }
    private String compute(String s) { return s; }
    private void run(int n) { }
}
