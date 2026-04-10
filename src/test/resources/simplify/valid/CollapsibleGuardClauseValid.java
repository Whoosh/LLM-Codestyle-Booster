package simplify.valid;

public class CollapsibleGuardClauseValid {

    private boolean exempt;
    private boolean important;
    private int count;

    private void hasStatementAfter() {
        if (exempt) {
            return;
        }
        if (important) {
            count++;
        }
        count *= 2;
    }

    private void guardWithReturnValue() {
        if (exempt) {
            return;
        }
        if (important) {
            count++;
        }
        count = 0;
    }

    private void guardHasElse() {
        if (exempt) {
            return;
        } else {
            count = -1;
        }
        if (important) {
            count++;
        }
    }

    private void secondIfHasElse() {
        if (exempt) {
            return;
        }
        if (important) {
            count++;
        } else {
            count--;
        }
    }

    private void guardBodyMultipleStatements() {
        if (exempt) {
            count = -1;
            return;
        }
        if (important) {
            count++;
        }
    }

    private void onlyOneIf() {
        if (important) {
            count++;
        }
    }

    private void guardReturnValueNotVoid() {
        if (exempt) {
            count = -1;
            return;
        }
    }

    private int nonVoidGuardReturn() {
        if (exempt) {
            return -1;
        }
        if (important) {
            return 1;
        }
        return 0;
    }

    private void firstStatementNotIf() {
        count = 0;
        if (important) {
            count++;
        }
    }

    private void secondStatementNotIf() {
        if (exempt) {
            return;
        }
        count++;
    }

    private void guardReturnsValueNonVoid() {
        if (exempt) {
            return;
        }
        count++;
        count *= 2;
    }
}
