package test;

public class IndexOfToContainsValid {

    public void check(String s, String sub) {
        // Using contains directly — valid
        if (!s.contains(sub)) {
            System.out.println("not found");
        }
        if (s.contains(sub)) {
            System.out.println("found");
        }
        // indexOf with fromIndex — NOT flagged (two arguments)
        int idx = s.indexOf(sub, 5);
        // Storing result for later use — NOT flagged
        int pos = s.indexOf(sub);
        if (pos > 2) {
            System.out.println("found after pos 2");
        }
        // 0 < indexOf — means "found after position 0", NOT equivalent to contains
        if (0 < s.indexOf(sub)) {
            System.out.println("found after start");
        }
    }

    public void charLiteralArg(String s) {
        // indexOf with char literal arg — isValidIndexOfArgument returns false (CHAR_LITERAL)
        // Exercises line 87: childType == CHAR_LITERAL
        if (s.indexOf('x') >= 0) {
            System.out.println("found char");
        }
    }

    public void methodCallArg(String s) {
        // indexOf with method call arg — isValidIndexOfArgument returns false (METHOD_CALL)
        // Exercises line 87: childType == METHOD_CALL
        if (s.indexOf(s.substring(0, 1)) >= 0) {
            System.out.println("found call result");
        }
    }

    public void singleCharIdentArg(String s, String c) {
        // indexOf with single-char variable name — isValidIndexOfArgument returns false
        // Exercises line 90: childType == IDENT && getText().length() == 1
        if (s.indexOf(c) >= 0) {
            System.out.println("found single-char var");
        }
    }
}
