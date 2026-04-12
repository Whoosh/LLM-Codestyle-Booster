package test;

public class BlankLineAfterCommentMultiBlock {

    // Case: multi-line block comment where closing line has only */
    /*
     * This is a multi-line block comment.
     * It spans several lines.
     */

    void afterBlockWithBlank() { }

    // Case: multi-line block comment where closing line has code after */
    /* start
       end */ // but with blank after

    void afterMixedBlock() { }
}
