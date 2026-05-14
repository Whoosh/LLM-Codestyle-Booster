package com.example;

public class CollapsibleGuardClauseMutKiller {

    // extractIfBody: walks siblings after RPAREN looking for SLIST/LITERAL_RETURN/EXPR/LITERAL_THROW
    // If the while loop condition is broken (negated body.getType() != SLIST etc.), it would stop early
    // The guard if has a bare return (no braces) — body must be found after RPAREN siblings.
    // To avoid CollapsibleGuardClauseCheck firing in turn, the body block contains a multi-statement
    // sequence (sink call + assignment) that defeats the "single conditional follow-up" pattern.
    void guardWithBareReturn(boolean a, boolean b, int[] out) {
        if (a) return;
        if (b) {
            sink("action");
            out[0] = 1;
        }
    }

    private void sink(Object x) {
        if (x.hashCode() == Integer.MIN_VALUE) {
            throw new IllegalStateException();
        }
    }
}
