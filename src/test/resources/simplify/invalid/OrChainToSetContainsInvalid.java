package simplify.invalid;

public class OrChainToSetContainsInvalid {

    public boolean isSmallPrime(int n) {
        return n == 2 || n == 3 || n == 5 || n == 7;
    }

    public boolean isLoopOrIfToken(int type) {
        return type == 10 || type == 20 || type == 30 || type == 40 || type == 50;
    }

    public boolean reversedOperandOrder(int type) {
        return 1 == type || 2 == type || 3 == type;
    }

    public boolean charLiteralChain(char ch) {
        return ch == 'a' || ch == 'b' || ch == 'c';
    }

    public boolean longLiteralChain(long value) {
        return value == 1L || value == 2L || value == 3L;
    }

    public boolean nestedDotLhs(int type, int parentType) {
        return parentType == 100 || parentType == 200 || parentType == 300;
    }

    public boolean equalsChainOnString(String name) {
        return name.equals("alpha") || name.equals("beta") || name.equals("gamma");
    }

    public boolean fiveEqualsCalls(String tag) {
        return tag.equals("a") || tag.equals("b") || tag.equals("c") || tag.equals("d") || tag.equals("e");
    }

    public boolean inLargerExpression(int type, boolean active) {
        return active && (type == 1 || type == 2 || type == 3);
    }

    public boolean upperSnakeCaseConstantRhs(int type) {
        return type == ALPHA || type == BETA || type == GAMMA || type == DELTA;
    }

    // Case: dotted path as LHS (exercises renderExpression DOT branch)
    public boolean dottedPathLhs(int[] arr) {
        return arr.length == 1 || arr.length == 2 || arr.length == 3;
    }

    // Case: method call as LHS (exercises renderExpression METHOD_CALL branch)
    public boolean methodCallLhs(String s) {
        return s.length() == 1 || s.length() == 2 || s.length() == 3;
    }

    // Case: this.field as LHS (exercises renderExpression LITERAL_THIS branch)
    private int field;
    public boolean thisFieldLhs() {
        return this.field == 10 || this.field == 20 || this.field == 30;
    }

    private static final int ALPHA = 1;
    private static final int BETA = 2;
    private static final int GAMMA = 3;
    private static final int DELTA = 4;
}
