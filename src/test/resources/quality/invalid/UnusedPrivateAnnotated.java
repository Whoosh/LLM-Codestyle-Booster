package com.example;

// Tests isPrivateNonAnnotated mutations (L109, L111) in UnusedPrivateMembersCheck
@SuppressWarnings("unused")
public class UnusedPrivateAnnotated {

    // Private method WITH @Override annotation — should NOT be flagged as unused
    // because isPrivateNonAnnotated returns false for @Override-annotated members
    @Override
    private void overrideMethod() { }

    // Private method WITHOUT annotations — SHOULD be flagged as unused
    private void unusedMethod() { }

    // Private field with annotation — tests ANNOTATION branch (L109)
    @Deprecated
    private String annotatedField = "test";

    // Private method with non-Override annotation — should still be flagged if unused
    @Deprecated
    private void deprecatedUnused() { }

    // Used reference to prevent false positive
    void user() {
        annotatedField = "used";
    }
}
