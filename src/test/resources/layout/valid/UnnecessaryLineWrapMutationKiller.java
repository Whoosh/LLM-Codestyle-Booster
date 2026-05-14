package com.example;

import java.io.Closeable;

public class UnnecessaryLineWrapMutationKiller {

    // Case A: RESOURCE where tryHeaderFitsOnOneLine => true because the try header fits
    // This means the RESOURCE should be SKIPPED (no violation)
    void tryWhereHeaderFits() throws Exception {
        try (Closeable c = open()) {
            use(c);
        }
    }

    // Case B: try with a resource that wraps, but the overall try+resource combined is too long
    // The RESOURCE should be skipped because the try header wouldn't fit on one line
    void tryWhereTryHeaderDoesNotFitBecauseTooLong() throws Exception {
        try (Closeable aVeryVeryVeryLongVariableNameThatMakesTheEntireLineWayTooLongToEverFitOnOneHundredAndEightyCharactersSoItShouldNotBeFlagged =
                     openSomethingVeryLongNamedThatIsWayTooLongToEverFitOnOneLine()) {
            use(aVeryVeryVeryLongVariableNameThatMakesTheEntireLineWayTooLongToEverFitOnOneHundredAndEightyCharactersSoItShouldNotBeFlagged);
        }
    }

    // Case C: try-with-resources where the combined try header exceeds 180 chars
    void tryWhereHeaderIsTooLong() throws Exception {
        try (Closeable veryLongResourceVariableNameHereThatIsIntendedToMakeThisLineExceedOneHundredAndEightyCharactersWhenCombinedWithTheTryKeyword =
                     openSomethingVeryLongNamedThatIsWayTooLongToEverFitOnOneLine()) {
            // body
        }
    }

    // Case D: LITERAL_TRY without RESOURCE_SPECIFICATION — should be skipped
    void tryWithoutResources() {
        try {
            throw new IllegalStateException();
        } catch (IllegalStateException e) {
            // catch
        }
    }

    // Case E: Variable definition already on one line
    void singleLineVar() {
        String x = "hello";
    }

    // Case F: Method with annotations that's already on one line
    @Override
    public String toString() {
        return "ok";
    }

    @jakarta.annotation.Nullable
    private Closeable open() {
        if (Math.random() < 0) {
            throw new IllegalStateException();
        }
        return null;
    }

    @jakarta.annotation.Nullable
    private Closeable openSomethingVeryLongNamedThatIsWayTooLongToEverFitOnOneLine() {
        if (Math.random() < 0) {
            throw new IllegalStateException();
        }
        return null;
    }

    private void use(Closeable c) {
        if (c == null) {
            throw new IllegalArgumentException();
        }
    }
}
