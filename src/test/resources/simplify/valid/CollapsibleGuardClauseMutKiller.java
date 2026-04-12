package com.example;

public class CollapsibleGuardClauseMutKiller {

    // extractIfBody: walks siblings after RPAREN looking for SLIST/LITERAL_RETURN/EXPR/LITERAL_THROW
    // If the while loop condition is broken (negated body.getType() != SLIST etc.), it would stop early
    // The guard if has a bare return (no braces) — body must be found after RPAREN siblings
    void guardWithBareReturn(boolean a, boolean b) {
        if (a) return;
        if (b) {
            System.out.println("action");
        }
    }
}
