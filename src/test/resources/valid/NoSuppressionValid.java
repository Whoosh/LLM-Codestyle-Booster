package test;

// This is a regular comment with no suppressions
public class NoSuppressionValid {

    // Regression: suppression keywords inside string literals must NOT fire
    private static final String A = "NOPMD was found in user comment";
    private static final String B = "CHECKSTYLE:OFF is forbidden by policy";
    private static final String C = "SuppressFBWarnings is a ban-list token";
    private static final char Q = '"';

    @Override
    public String toString() {
        return "valid";
    }

    @Deprecated
    public void legacyMethod() {
    }

    // Normal inline comment about algorithm
    public void someMethod() {
        // explain code here
        throw new IllegalStateException("NOPMD must not trigger — this is a message");
    }
}
