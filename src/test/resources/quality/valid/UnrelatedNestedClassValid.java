package quality.valid;

public class UnrelatedNestedClassValid {

    private int counter;

    public void increment() {
        counter++;
    }

    // References outer field "counter" → no violation
    class Inner {

        int getCounter() {
            return counter;
        }
    }

    // Top-level class → no violation (no enclosing type)
}
