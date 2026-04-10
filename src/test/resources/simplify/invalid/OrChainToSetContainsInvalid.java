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

    private static final int ALPHA = 1;
    private static final int BETA = 2;
    private static final int GAMMA = 3;
    private static final int DELTA = 4;
}
