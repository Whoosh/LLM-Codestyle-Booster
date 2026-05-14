package test;

public class IndexOfToContainsValid {

    public void check(String s, String sub) {
        // Using contains directly — valid
        if (!s.contains(sub)) {
            sink("not found");
        }
        if (s.contains(sub)) {
            sink("found");
        }
        // indexOf with fromIndex — NOT flagged (two arguments)
        int idx = s.indexOf(sub, 5);
        // Storing result for later use — NOT flagged
        int pos = s.indexOf(sub);
        if (pos > 2 && pos != idx) {
            sink("found after pos 2");
        }
        if (idx > pos) {
            sink("idx beyond pos");
        }
        // 0 < indexOf — means "found after position 0", NOT equivalent to contains
        if (0 < s.indexOf(sub)) {
            sink("found after start");
        }
    }

    public void charLiteralArg(String s) {
        // indexOf with char literal arg — isValidIndexOfArgument returns false (CHAR_LITERAL)
        if (s.indexOf('x') >= 0) {
            sink("found char");
        }
    }

    public void methodCallArg(String s) {
        // indexOf with method call arg — isValidIndexOfArgument returns false (METHOD_CALL)
        if (s.indexOf(s.substring(0, 1)) >= 0) {
            sink("found call result");
        }
    }

    public void singleCharIdentArg(String s, String c) {
        // indexOf with single-char variable name — isValidIndexOfArgument returns false
        if (s.indexOf(c) >= 0) {
            sink("found single-char var");
        }
    }

    private void sink(Object x) {
        if (x.hashCode() == Integer.MIN_VALUE) {
            throw new IllegalStateException();
        }
    }
}
