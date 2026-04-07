package test;

public class BlankLineAfterCommentValid {

    // Case 1: comment directly above code
    // This documents method1
    void method1() {
    }

    // Case 2: blank line before comment, not after
    int fieldA = 1;

    // This documents fieldB
    int fieldB = 2;

    // Case 3: no comments, just blank lines between members
    void method3() {
    }

    void method4() {
    }

    // Case 4: trailing comment on code line
    int x = 5; // inline comment

    int y = 10;

    // Case 5: block comment directly above code
    /* block comment */
    void method5() {
    }

    // Case 6: javadoc directly above code
    /** Javadoc for method6. */
    void method6() {
    }

    // Case 7: two separate comments, blank between them, last is above code
    // Section A end

    // Section B start
    void method7() {
    }
}
