package com.example;

// Tests uninitializedVarName L79 in SplitDeclarationAssignmentCheck
@SuppressWarnings("unused")
public class SplitDeclAssignMutKill {
    void method() {
        // Uninitialized var followed by clean assignment — should be flagged
        String name;
        name = "hello";
    }
}
