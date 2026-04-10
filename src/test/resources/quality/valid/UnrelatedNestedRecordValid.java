package quality.valid;

public class UnrelatedNestedRecordValid {

    private int counter;

    public void increment() {
        counter++;
    }

    public record CounterSnapshot(int value) {

        public boolean exceeds(UnrelatedNestedRecordValid outer) {
            return value > outer.counter;
        }
    }

    private record IncrementCommand(int by) {

        public void apply(UnrelatedNestedRecordValid target) {
            for (int i = 0; i < by; i++) {
                target.increment();
            }
        }
    }

    // Records nested 2 levels deep that DO reference their immediate enclosing
    // type's members must NOT be flagged.
    static class InnerHolder {

        int innerField;

        record DeepReferencing(int v) {

            public int total(InnerHolder host) {
                return v + host.innerField;
            }
        }
    }
}
