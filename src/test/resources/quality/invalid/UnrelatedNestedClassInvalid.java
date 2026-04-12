package quality.invalid;

public class UnrelatedNestedClassInvalid {

    private int counter;

    public void increment() {
        counter++;
    }

    // Nested class with only static method, no outer references → violation
    static class Helper {

        static String format(String s) {
            return s.trim();
        }
    }

    // Data-carrier class, no outer references → violation
    static final class Stats {

        private int totalRows;
        private int totalParsed;
    }

    // Nested class inside an interface → violation
    interface Config {

        class Defaults {

            static final int TIMEOUT = 30;
        }
    }
}
