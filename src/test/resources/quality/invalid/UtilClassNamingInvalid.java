package quality.invalid;

public class UtilClassNamingInvalid {

    // Util-shaped class with a non-utility name → must be renamed to *Util(s).
    public static class StringHelper {

        public static String trim(String s) {
            return s.strip();
        }

        public static String upper(String s) {
            return s.toUpperCase();
        }
    }

    // Constants holder with a public method → forbidden.
    public static class AppConstants {

        public static final String NAME = "x";

        public static String getName() {
            return NAME;
        }
    }

    // Public + package-private mix, all static, name "Stuff" → must be renamed.
    public static class Stuff {

        public static int compute() {
            return 1;
        }

        static int helper() {
            return 2;
        }
    }
}
