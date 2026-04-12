package com.example;

// Tests followingReturnInBlock L78 in IfReturnBooleanLiteralCheck
// The mutation on L78 negates: `siblingType != SEMI && siblingType != RCURLY`
// If negated: SEMI/RCURLY siblings would return null, so the check wouldn't fire.
@SuppressWarnings("unused")
public class IfReturnBoolLitSemiKill {
    // After the if, there's a SEMI token (;) and RCURLY before the `return false` statement.
    // The check must skip SEMI and RCURLY to find the return statement.
    boolean method(boolean cond) {
        if (cond) {
            return true;
        }
        return false;
    }
}
