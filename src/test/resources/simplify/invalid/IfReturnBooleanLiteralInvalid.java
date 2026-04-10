package simplify.invalid;

public class IfReturnBooleanLiteralInvalid {

    private int value;

    public boolean trueFalse() {
        if (value > 0) {
            return true;
        }
        return false;
    }

    public boolean falseTrue() {
        if (value > 0) {
            return false;
        }
        return true;
    }

    public boolean braceless() {
        if (value > 0) return true;
        return false;
    }

    public boolean withPriorStmts() {
        value++;
        if (value > 5) {
            return true;
        }
        return false;
    }
}
