package quality.invalid;

public class ClassMayBeRecordInvalid {

    // Pure data carrier — 3 private fields, no methods → violation
    static final class Stats {

        private int totalRows;
        private int totalParsed;
        private int skippedSuspicious;
    }

    // Data carrier with constructor → violation
    static final class Pair {

        private final String key;
        private final String value;

        Pair(String key, String value) {
            this.key = key;
            this.value = value;
        }
    }

    // Data carrier with standard methods only → violation
    static final class Point {

        private int x;
        private int y;

        @Override
        public String toString() {
            return "Point(" + x + ", " + y + ")";
        }

        @Override
        public int hashCode() {
            return x * 31 + y;
        }
    }

    // Top-level final class with only private fields → violation
}

final class TopLevelDataCarrier {

    private String name;
    private int age;
}
