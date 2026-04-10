package quality.invalid;

public class MethodMayBeStaticInvalid {

    private static final int MAGIC = 42;

    private final int instanceField = 1;

    // Uses only parameters → may be static.
    private int sum(int a, int b) {
        return a + b;
    }

    // Uses only static state → may be static.
    private int boundedToMagic(int v) {
        return Math.min(v, MAGIC);
    }

    // Returns a literal → may be static.
    private String defaultLabel() {
        return "default";
    }

    // Calls another static helper only → may be static.
    private boolean isPositive(int x) {
        return Math.signum(x) > 0;
    }

    // Uses local var only → may be static.
    private int compute(int seed) {
        int local = seed * 2;
        return local + 1;
    }

    public int useAll(int a, int b) {
        return sum(a, b) + boundedToMagic(a) + defaultLabel().length() + (isPositive(a) ? 0 : 1) + compute(b) + instanceField;
    }
}
