package test;

public class UnusedPrivateMembersNested {

    // Used field
    private int usedField = 42;

    public int getUsedField() {
        return usedField;
    }

    // Nested class with unused private member
    // collectAllPrivateDeclarations must recurse into nested types (line 76-77)
    static class Inner {
        // Unused private field inside nested class — should be flagged
        private String nestedUnused = "orphan";

        // Used field inside nested class — not flagged
        private int nestedUsed = 1;

        public int getNestedUsed() {
            return nestedUsed;
        }
    }

    // Private method with @Override annotation — should NOT be flagged
    // isPrivateNonAnnotated returns false when @Override is found (lines 109-111)
    @Override
    public String toString() {
        return "test";
    }

    // Private method with non-Override annotation — SHOULD be flagged if unused
    @Deprecated
    private void annotatedButNotOverride() { }
}
