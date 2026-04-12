package test;

// Exercises edge cases for StaticImportCandidateCheck:
// - isUpperCaseConstant with underscore-only names (line 162)
// - isInsideImport DOT inside import (line 176)
// - findLastIdent with STAR import (line 118)
// - finishTree with ambiguous constant names from different classes (line 77)

import static java.lang.Math.*;

public class StaticImportCandidateMutationKiller {

    // Two classes define VALUE — ambiguous, should only fire for the one with more refs
    public void ambiguousConstants() {
        int a = Alpha.VALUE;
        int b = Beta.VALUE;
        int c = Beta.VALUE;
    }

    // Underscore-only name should NOT be treated as upper-case constant
    public void underscoreOnlyNotConstant() {
        int x = Gamma.____;
    }

    // Digit-only names should NOT be treated as upper-case constant
    // (isUpperCaseConstant requires at least one uppercase letter)

    // Constant with mixed digits and underscores but at least one uppercase
    public void mixedConstant() {
        int y = Delta.V_1;
        int z = Delta.V_1;
    }

    static class Alpha {
        public static final int VALUE = 1;
    }

    static class Beta {
        public static final int VALUE = 2;
    }

    static class Gamma {
        public static final int ____ = 0;
    }

    static class Delta {
        public static final int V_1 = 3;
    }
}
