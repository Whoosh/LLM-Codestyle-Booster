package simplify.valid;

public class OrChainToSetContainsValid {

    public boolean twoOperandsOnly(int type) {
        return type == 1 || type == 2;
    }

    public boolean differentLhs(int a, int b, int c) {
        return a == 1 || b == 2 || c == 3;
    }

    public boolean mixedOperators(int x) {
        return x == 1 || x > 10 || x == 3;
    }

    public boolean literalVsNonLiteral(int x, int y) {
        return x == 1 || x == y || x == 3;
    }

    public boolean stringEqualityViaEqSign(String s) {
        return s == "a" || s == "b" || s == "c";
    }

    public boolean booleanLiteralChain(boolean flag) {
        return flag == true || flag == false;
    }

    public boolean nullCheckChain(Object obj) {
        return obj == null || obj == this || obj == "";
    }

    public boolean mixedShapeNotFlagged(int type, String name) {
        return type == 1 || name.equals("x") || type == 3;
    }

    public boolean equalsCallDifferentReceiver(String a, String b) {
        return a.equals("x") || a.equals("y") || b.equals("z");
    }

    public boolean equalsCallWithNonLiteralArg(String s, String ref) {
        return s.equals("x") || s.equals(ref) || s.equals("z");
    }

    public boolean equalsCallDifferentMethod(String s) {
        return s.equals("x") || s.startsWith("y") || s.endsWith("z");
    }

    public boolean notEqualsChain(int type) {
        return type != 1 || type != 2 || type != 3;
    }

    public boolean twoCharsOnly(char ch) {
        return ch == 'a' || ch == 'b';
    }

    public boolean compileTimeConstantsNotLiterals(int type) {
        final int limit = 10;
        return type == limit || type == limit + 1 || type == limit + 2;
    }
}
