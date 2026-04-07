package test;

public class ForbidAssertKeywordValid {

    void checkWithException(Object obj) {
        if (obj == null) {
            throw new IllegalArgumentException("obj must not be null");
        }
    }

    void checkWithPreconditionsStyle(String s) {
        if (s.isEmpty()) {
            throw new IllegalStateException("must not be empty");
        }
    }

    // "assert" in a string literal is fine
    void stringContainingAssert() {
        String msg = "Use assert for testing";
    }
}
