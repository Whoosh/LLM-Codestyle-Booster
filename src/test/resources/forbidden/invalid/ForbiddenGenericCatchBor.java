package com.example;

// Tests BOR branch in checkChild — RuntimeException is the ONLY forbidden type,
// and it appears AFTER the pipe in a multi-catch. If BOR recursion is broken,
// RuntimeException inside BOR won't be detected.
@SuppressWarnings("unused")
public class ForbiddenGenericCatchBor {
    void methodA() {
        try {
            throw new RuntimeException();
        } catch (IllegalArgumentException | RuntimeException e) {
            // multi-catch: safe type | forbidden type
        }
    }

    void methodB() {
        try {
            throw new RuntimeException();
        } catch (IllegalStateException | Throwable e) {
            // multi-catch: safe type | forbidden Throwable
        }
    }
}
