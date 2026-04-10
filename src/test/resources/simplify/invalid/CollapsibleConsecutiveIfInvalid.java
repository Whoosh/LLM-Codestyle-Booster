package simplify.invalid;

public class CollapsibleConsecutiveIfInvalid {

    public int pairOfReturnVoid(int parentType) {
        if (parentType == 1) {
            return 10;
        }
        if (parentType == 2) {
            return 10;
        }
        return 0;
    }

    public void pairOfPlainReturn(Object ast) {
        if (ast == null) {
            return;
        }
        if (ast.hashCode() == 0) {
            return;
        }
        doWork();
    }

    public int tripleReturn(int x) {
        if (x == 1) {
            return -1;
        }
        if (x == 2) {
            return -1;
        }
        if (x == 3) {
            return -1;
        }
        return x;
    }

    public void pairOfThrow(int x) {
        if (x < 0) {
            throw new IllegalArgumentException("bad");
        }
        if (x > 1000) {
            throw new IllegalArgumentException("bad");
        }
        doWork();
    }

    public void pairOfContinueInLoop(int[] xs) {
        for (int x : xs) {
            if (x < 0) {
                continue;
            }
            if (x == 42) {
                continue;
            }
            handle(x);
        }
    }

    public void pairOfBreakInLoop(int[] xs) {
        for (int x : xs) {
            if (x < 0) {
                break;
            }
            if (x == 42) {
                break;
            }
            handle(x);
        }
    }

    public int noBracesStillFlagged(int x) {
        if (x == 1) return -1;
        if (x == 2) return -1;
        return x;
    }

    public int identicalExpressionReturns(int x, int y) {
        if (x > 0) {
            return x + y;
        }
        if (y > 0) {
            return x + y;
        }
        return 0;
    }

    public void nestedInner(boolean outer, boolean a, boolean b) {
        if (outer) {
            if (a) {
                return;
            }
            if (b) {
                return;
            }
            doWork();
        }
    }

    private void doWork() {
    }

    private void handle(int x) {
    }
}
