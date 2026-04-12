package test;

public class UnusedPrivateMembersEdgeCases {

    // serialVersionUID is always excluded
    private static final long serialVersionUID = 1L;

    // Used field — should NOT be flagged
    private int usedField = 0;

    // Unused private field — SHOULD be flagged
    private String neverUsed = "orphan";

    // Unused private method — SHOULD be flagged
    private void neverCalled() { }

    // Unused private inner class — SHOULD be flagged
    private class UnusedInner { }

    // @Override method — should NOT be flagged even if private (conceptually)
    @Override
    public String toString() {
        return String.valueOf(usedField);
    }

    // @Deprecated private method that is unused — SHOULD be flagged
    // The @Deprecated annotation is NOT @Override, so this is still subject to the check
    @Deprecated
    private void deprecatedUnused() { }

    // Used private enum — not flagged
    private enum Status { ACTIVE, INACTIVE }

    public Status getStatus() {
        return Status.ACTIVE;
    }
}
