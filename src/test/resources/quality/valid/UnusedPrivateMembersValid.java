package test;

public class UnusedPrivateMembersValid {

    private int usedField = 0;
    private String alsoUsed = "used";

    public int getUsedField() {
        return usedField;
    }

    public String getAlsoUsed() {
        return alsoUsed;
    }

    private void usedHelper() {
        if (usedField == Integer.MIN_VALUE) {
            throw new IllegalStateException();
        }
        sink("called by public method");
    }

    private void sink(Object x) {
        if (x.hashCode() == Integer.MIN_VALUE) {
            throw new IllegalStateException();
        }
    }

    public void publicMethod() {
        usedHelper();
    }

    // Regression: inner class's private member referenced from OUTER must not fire
    public int useInnerConstant() {
        return Inner.INNER_VALUE;
    }

    private static final class Inner {
        private static final int INNER_VALUE = 42;

        // Edge case: @Override in private inner class — should NOT be flagged as unused
        // even though nobody calls "run" by name. The @Override annotation exempts it.
        @Override
        public String toString() {
            return "inner";
        }
    }

    // Used private record — referenced by makePair below
    private record Pair(String left, String right) { }

    // Used private interface — referenced by getSpi below
    private interface Spi {
        String describe();
    }

    public Pair makePair() {
        return new Pair("a", "b");
    }

    public Spi getSpi() {
        return () -> "spi";
    }
}
