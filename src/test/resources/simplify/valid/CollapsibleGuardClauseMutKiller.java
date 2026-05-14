package com.example;

public class CollapsibleGuardClauseMutKiller {

    // extractIfBody: walks siblings after RPAREN looking for SLIST/LITERAL_RETURN/EXPR/LITERAL_THROW
    // If the while loop condition is broken (negated body.getType() != SLIST etc.), it would stop early.
    // The guard if has a bare return (no braces) — body must be found after RPAREN siblings.
    // The if(b) block has multi-statement body AND is followed by another statement, so
    // CollapsibleGuardClauseCheck does not match the "guard + single conditional terminating block" pattern.
    void guardWithBareReturn(boolean a, boolean b, int[] out) {
        if (a) return;
        if (b) {
            sink("action");
            out[0] = 1;
        }
        out[0] = out[0] + 1;
    }

    private void sink(Object x) {
        if (x.hashCode() == Integer.MIN_VALUE) {
            throw new IllegalStateException();
        }
    }
}
