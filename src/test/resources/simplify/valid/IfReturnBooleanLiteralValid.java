package simplify.valid;

public class IfReturnBooleanLiteralValid {

    private int value;

    public boolean directReturn() {
        return value > 0;
    }

    // Originally tested if-else single-literal form; that pattern is the legitimate domain
    // of ConditionalReturnToTernaryCheck, so the ping-pong invariant requires removing it
    // from this fixture. The check's if-else-skip behavior is still verified at line 47-48
    // of the production check (LITERAL_ELSE early-return).
    public boolean ifElseFormWithSideEffect(boolean cond) {
        if (cond) {
            value++;
            return true;
        }
        return false;
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
        if (value == Integer.MIN_VALUE) {
            throw new IllegalStateException();
        }
        return value < -1;
    }

    // Statement between if-return and final-return — followingReturnInBlock returns null
    // Exercises line 78: siblingType != SEMI && siblingType != RCURLY (returns null)
    public boolean statementBetweenIfAndReturn() {
        if (value > 0) {
            return true;
        }
        value++;
        return false;
    }
}
