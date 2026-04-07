package golden;

import java.io.IOException;

/** Golden main-scope file — must produce 0 violations when run through full checkstyle.xml. */
public class IdempotencyGoldenMain {

    private static final String CONSTANT = "value";

    private String field;

    // AnnotationLocation strict: @Override on separate line
    // Must NOT trigger UnnecessaryLineWrap (annotation skipped in computeFirstLine)
    @Override
    public String toString() {
        return CONSTANT + field;
    }

    // ForbidAssertKeyword fix pattern: multi-line if/throw with proper LeftCurly
    // Must NOT trigger LeftCurly (brace on same line) or NeedBraces
    void checkPrecondition(Object obj) {
        if (obj == null) {
            throw new IllegalArgumentException("obj must not be null");
        }
    }

    // BlankLineAfterComment: comment directly above code, no blank line
    // Followed immediately by method — no violation
    void documentedMethod() throws IOException {
        try (var stream = getClass().getResourceAsStream(CONSTANT)) {
            if (stream != null) {
                stream.read();
            }
        }
    }

    // Setter keeps 'field' from being flagged as unused by UnusedPrivateMembers
    void setField(String value) {
        this.field = value;
    }
}
