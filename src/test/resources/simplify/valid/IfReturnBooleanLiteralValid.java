package simplify.valid;

public class IfReturnBooleanLiteralValid {

    private int value;

    public boolean directReturn() {
        return value > 0;
    }

    public boolean ifElseForm(boolean cond) {
        if (cond) {
            return true;
        } else {
            return false;
        }
    }

    public boolean sameLiteralBoth() {
        if (value > 0) {
            return true;
        }
        return true;
    }

    public boolean nonLiteralReturn() {
        if (value > 0) {
            return value % 2 == 0;
        }
        return false;
    }

    public boolean ifBodyMultipleStmts() {
        if (value > 0) {
            value--;
            return true;
        }
        return false;
    }

    public int notBoolean() {
        if (value > 0) {
            return 1;
        }
        return 0;
    }

    public boolean returnOfMethod() {
        if (value > 0) {
            return true;
        }
        return computeFallback();
    }

    private boolean computeFallback() {
        return value < -1;
    }
}
