package quality.valid;

public class UnrelatedNestedInterfaceValid {

    static final int MAX_SIZE = 100;

    // Default method references outer constant MAX_SIZE → no violation
    interface Limiter {

        default boolean isWithinLimit(int size) {
            return size <= MAX_SIZE;
        }
    }
}
