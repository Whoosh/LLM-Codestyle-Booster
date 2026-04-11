package quality.valid;

public class UnrelatedNestedEnumValid {

    static final int DEFAULT_LIMIT = 100;

    private int counter;

    public void increment() {
        counter++;
    }

    // Valid: Policy.maxAttempts() references outer static constant DEFAULT_LIMIT.
    public enum Policy {
        STRICT, LAX;

        public int maxAttempts() {
            return DEFAULT_LIMIT;
        }
    }

    // Valid: Operation.apply takes the outer type and calls its increment() method —
    // the method-call IDENT 'increment' matches an outer member.
    public enum Operation {
        TICK, RESET;

        public void apply(UnrelatedNestedEnumValid host) {
            if (this == TICK) {
                host.increment();
            }
        }
    }
}
