package simplify.invalid;

public class CollapsibleNestedIfInvalid {

    private boolean a;
    private boolean b;
    private int counter;

    public void simpleCase() {
        if (a) {
            if (b) {
                counter++;
            }
        }
    }

    public void singleStatementInner() {
        if (a) {
            if (b) {
                counter++;
                counter *= 2;
            }
        }
    }

    public void deeplyNested() {
        if (a) {
            if (b) {
                if (counter > 0) {
                    counter--;
                }
            }
        }
    }
}
