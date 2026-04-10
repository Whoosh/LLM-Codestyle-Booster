package simplify.valid;

public class BooleanFromConditionValid {

    private int value;

    public boolean directInit() {
        boolean positive = value > 0;
        return positive;
    }

    public boolean withElse() {
        boolean positive = false;
        if (value > 0) {
            positive = true;
        } else {
            value++;
            positive = true;
        }
        return positive;
    }

    public boolean assignmentNotALiteralFlip() {
        boolean state = false;
        if (value > 0) {
            state = value > 5;
        }
        return state;
    }

    public boolean multipleStatementsInIf() {
        boolean flag = false;
        if (value > 0) {
            value++;
            flag = true;
        }
        return flag;
    }

    public boolean nonBooleanType() {
        int x = 0;
        if (value > 0) {
            x = 1;
        }
        return x > 0;
    }

    public boolean intermediateStatementBetween() {
        boolean flag = false;
        value++;
        if (value > 0) {
            flag = true;
        }
        return flag;
    }

    public boolean assignmentToOtherVar() {
        boolean flag = false;
        boolean other;
        if (value > 0) {
            other = true;
        } else {
            other = false;
        }
        return flag || other;
    }

    public boolean differentLiteralPair() {
        boolean flag = true;
        if (value > 0) {
            flag = true;
        }
        return flag;
    }

    public boolean initWithExpression() {
        boolean flag = isInitialized();
        if (value > 0) {
            flag = true;
        }
        return flag;
    }

    private boolean isInitialized() {
        return value != 0;
    }
}
