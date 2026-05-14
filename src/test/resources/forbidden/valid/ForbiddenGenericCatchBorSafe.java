package com.example;

// Multi-catch with BOR that does NOT contain any forbidden types
public class ForbiddenGenericCatchBorSafe {
    void method() {
        try {
            throw new Exception();
        } catch (IllegalArgumentException | IllegalStateException e) {
            // safe multi-catch
        }
    }
}
