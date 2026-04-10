package simplify.invalid;

public class BooleanFromConditionInvalid {

    private int value;

    public boolean isPositive() {
        boolean positive = false;
        if (value > 0) {
            positive = true;
        }
        return positive;
    }

    public boolean isNonZero() {
        boolean nonZero = true;
        if (value == 0) {
            nonZero = false;
        }
        return nonZero;
    }

    public boolean withSingleStatement() {
        boolean flag = false;
        if (value > 5) flag = true;
        return flag;
    }

    public void inMiddleOfBlock() {
        value = 0;
        boolean done = false;
        if (value > 0) {
            done = true;
        }
        value = done ? 1 : -1;
    }
}
