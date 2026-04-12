package test;

public class BlankLineAfterCommentBlockBody {

    // Multi-line block comment where body line has */ with trailing content — no blank line violation
    /* start of comment
       body continues
       end of comment */ void afterBlockBody() { }

    // Multi-line block comment where body line has */ with nothing after — then blank line
    // This should be detected as a comment, but there's code immediately so no violation
    /* start
       body
       end */
    void afterCleanEnd() { }
}
