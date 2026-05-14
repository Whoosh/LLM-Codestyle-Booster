package simplify.valid;

public class SplitDeclarationAssignmentValid {

    public int directInit() {
        int x = 5;
        x++;
        return x;
    }

    public int conditionalAssignment(boolean cond) {
        int x;
        if (cond) {
            x = 1;
        } else {
            x = 2;
        }
        return x;
    }

    public int controlFlowBetween(boolean cond) {
        int x;
        if (cond) {
            return -1;
        }
        x = 5;
        return x;
    }

    public int loopBetween() {
        int x;
        for (int i = 0; i < 10; i++) {
            doSomething();
        }
        x = 42;
        return x;
    }

    public int tryBetween() {
        int x;
        try {
            doSomething();
        } catch (IllegalStateException ignored) {
            // log
        }
        x = 1;
        return x;
    }

    public int returnBetween(boolean flag) {
        int x;
        if (flag) {
            return -1;
        }
        x = 5;
        return x;
    }

    public int compoundAssignment() {
        int x = 0;
        x += 5;
        return x;
    }

    public int assignedInLoopBody() {
        int x;
        for (int i = 0; i < 3; i++) {
            x = i;
            doSomething();
        }
        return 0;
    }

    public int assignedInsideTry() {
        int x;
        try {
            x = parse();
        } catch (IllegalArgumentException e) {
            x = -1;
        }
        return x;
    }

    private int parse() {
        if (Math.random() < 0) {
            throw new IllegalStateException();
        }
        return 1;
    }

    private void doSomething() {
        // no-op
    }
}
