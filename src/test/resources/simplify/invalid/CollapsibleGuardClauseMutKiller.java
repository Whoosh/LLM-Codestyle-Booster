package com.example;

public class CollapsibleGuardClauseMutKiller {

    // extractIfBody: exercises the while loop that walks siblings after RPAREN
    // The guard if uses bare return without braces — RPAREN's nextSibling traversal needed
    void guardWithBareReturn(boolean a, boolean b) {
        if (a) return;
        if (b) {
            System.out.println("action");
        }
    }
}
