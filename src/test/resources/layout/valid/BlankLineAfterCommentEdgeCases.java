package test;

public class BlankLineAfterCommentEdgeCases {

    // Case: multi-line block comment without blank line after
    /*
     * This is a multi-line
     * block comment that spans
     * multiple lines.
     */
    void afterMultiLineBlock() { }

    // Case: block comment with code right after closing on same line
    /* comment */ void inlineBlockComment() { }

    // Case: block comment start without close on same line, code right after
    /*
     * multi-line block
     */
    void afterUnclosedBlock() { }

    // No blank line here
    void afterComment() { }
}
