package simplify.invalid;

public class CollapsibleGuardClauseInvalid {

    private boolean exempt;
    private boolean important;
    private int count;

    private void simpleCase() {
        if (exempt) {
            return;
        }
        if (important) {
            count++;
        }
    }

    private void singleLineGuard() {
        if (exempt) return;
        if (important) {
            count++;
        }
    }

    private void singleLineSecondIf() {
        if (exempt) {
            return;
        }
        if (important) count++;
    }

    private void bothSingleLine() {
        if (exempt) return;
        if (important) count++;
    }

    private void complexBodyInSecondIf() {
        if (exempt) {
            return;
        }
        if (important) {
            count++;
            count *= 2;
            count -= 1;
        }
    }
}
