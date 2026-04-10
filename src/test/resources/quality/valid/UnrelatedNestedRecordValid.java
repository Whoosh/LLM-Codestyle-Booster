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
}
