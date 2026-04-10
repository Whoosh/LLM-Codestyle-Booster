package simplify.valid;

public class CollapsibleNestedIfValid {

    private boolean a;
    private boolean b;
    private int counter;

    public void outerHasElse() {
        if (a) {
            if (b) {
                counter++;
            }
        } else {
            counter--;
        }
    }

    public void innerHasElse() {
        if (a) {
            if (b) {
                counter++;
            } else {
                counter--;
            }
        }
    }

    public void outerHasOtherStatements() {
        if (a) {
            counter = 0;
            if (b) {
                counter++;
            }
        }
    }

    public void innerNotImmediate() {
        if (a) {
            counter++;
        }
    }

    public void elseIfChain() {
        if (a) {
            counter++;
        } else if (b) {
            counter--;
        }
    }

    public void twoSequentialIfs() {
        if (a) {
            counter++;
        }
        if (b) {
            counter--;
        }
    }

    public void singleIfNotNested() {
        if (a && b) {
            counter++;
        }
    }
}
