package quality.invalid;

public class UnrelatedNestedEnumInvalid {

    private int counter;

    public void increment() {
        counter++;
    }

    // ERROR: pure constants, no reference to counter or increment.
    public enum Color {
        RED, GREEN, BLUE
    }

    // ERROR: method body uses only enum-internal state (this/ordinal).
    private enum Size {
        SMALL, MEDIUM, LARGE;

        public boolean isBigger(Size other) {
            return this.ordinal() > other.ordinal();
        }
    }

    // ERROR: empty package-private enum.
    enum Mode {
        ON, OFF
    }

    // ERROR: nested inside an empty interface — outer has no members at all.
    interface Inner {

        enum Direction {
            UP, DOWN
        }
    }

    // ERROR: nested 2 levels deep; the immediate enclosing class has only holderField
    // and the Status enum never touches it.
    static class Level2Holder {

        int holderField;

        public enum Status {
            ACTIVE, INACTIVE;

            public int statusCode() {
                return this.ordinal();
            }
        }
    }
}
