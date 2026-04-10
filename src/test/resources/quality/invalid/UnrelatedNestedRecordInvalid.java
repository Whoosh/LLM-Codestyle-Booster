package quality.invalid;

public class UnrelatedNestedRecordInvalid {

    private int counter;

    public void increment() {
        counter++;
    }

    public record Pair(int a, int b) {
    }

    private record Coords(double x, double y) {

        public double magnitude() {
            return Math.sqrt(x * x + y * y);
        }
    }

    record PackagePrivatePoint(int x, int y) {
    }

    interface Inner {

        record Vector(double dx, double dy) {
        }
    }

    enum Mode {
        ON, OFF;

        record Snapshot(String mode, long timestamp) {
        }
    }

    // Records are implicitly static — `this` refers to the record itself, not outer.
    // The check used to be fooled by LITERAL_THIS and silently skipped these.
    public record SelfReferencingRecord(int x, int y) {

        public boolean equalsByX(SelfReferencingRecord other) {
            return this.x == other.x;
        }

        public int hashed() {
            return this == null ? 0 : x * y;
        }
    }

    // Deeply nested record: 2 levels under the outer class.
    static class Level2Holder {

        int holderField;

        public record Level2Record(int a, int b) {

            public int product() {
                return this.a * this.b;
            }
        }
    }
}
