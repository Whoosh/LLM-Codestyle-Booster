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
}
