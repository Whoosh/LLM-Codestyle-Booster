package test;

public class BlankLineAfterCommentInvalid {

    // Case 1: single-line comment with blank line before code
    // This comment documents the method

    void method1() {
    }

    // Case 2: consecutive comment block with blank line before code
    // First line of comment
    // Second line of comment

    void method2() {
    }

    // Case 3: block comment with blank line
    /* block comment */

    void method3() {
    }

    // Case 4: javadoc with blank line
    /** Javadoc comment. */

    void method4() {
    }

    // Case 5: section separator comment with blank line
    // --- section ---

    void method5() {
    }
}
