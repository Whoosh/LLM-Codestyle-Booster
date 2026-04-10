package simplify.valid;

public class CollapsibleConsecutiveIfValid {

    public int differentReturnValues(int x) {
        if (x == 1) {
            return 10;
        }
        if (x == 2) {
            return 20;
        }
        return 0;
    }

    public void nonTerminatingBody(int x, int[] out) {
        if (x == 1) {
            out[0]++;
        }
        if (x == 2) {
            out[0]++;
        }
    }

    public void firstHasElse(int x) {
        if (x == 1) {
            return;
        } else {
            doWork();
        }
        if (x == 2) {
            return;
        }
    }

    public void secondHasElse(int x) {
        if (x == 1) {
            return;
        }
        if (x == 2) {
            return;
        } else {
            doWork();
        }
    }

    public void multiStatementTerminating(int x) {
        if (x == 1) {
            doWork();
            return;
        }
        if (x == 2) {
            doWork();
            return;
        }
    }

    public int differentThrowTypes(int x) {
        if (x == 1) {
            throw new IllegalArgumentException("a");
        }
        if (x == 2) {
            throw new IllegalStateException("a");
        }
        return x;
    }

    public int throwWithDifferentMessages(int x) {
        if (x == 1) {
            throw new IllegalArgumentException("a");
        }
        if (x == 2) {
            throw new IllegalArgumentException("b");
        }
        return x;
    }

    public int intermediateStatement(int x) {
        if (x == 1) {
            return -1;
        }
        doWork();
        if (x == 2) {
            return -1;
        }
        return x;
    }

    public void elseIfChainNotFlagged(int x) {
        if (x == 1) {
            return;
        } else if (x == 2) {
            return;
        }
        doWork();
    }

    public void nestedIfInBodyNotFlagged(int x) {
        if (x == 1) {
            if (x == 2) {
                return;
            }
        }
        if (x == 3) {
            return;
        }
    }

    public void adjacentIfsBothNonTerminating(boolean a, boolean b, int[] out) {
        if (a) {
            out[0] = 1;
        }
        if (b) {
            out[0] = 1;
        }
    }

    public int returnFieldAccessMismatched(int x) {
        if (x == 1) {
            return this.sentinel;
        }
        if (x == 2) {
            return this.other;
        }
        return x;
    }

    public int sentinel = -1;
    public int other = -2;

    private void doWork() {
    }
}
