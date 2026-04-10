package quality.valid;

public class UtilClassNamingValid {

    // Pure constants holder, no methods → OK.
    public static class Constants {

        public static final String NAME = "x";

        public static final int LIMIT = 100;
    }

    // Constants holder with non-public method → still OK (rule 2 only forbids
    // *public* methods).
    public static class InternalConstants {

        public static final String FOO = "f";

        static int defaultLimit() {
            return 1;
        }
    }

    // Util-named class with all-static public methods → OK.
    public static class StringUtil {

        public static String trim(String s) {
            return s.strip();
        }
    }

    // Utils-named class with all-static public methods → OK.
    public static class CollectionUtils {

        public static int sumSize(java.util.List<?> a, java.util.List<?> b) {
            return a.size() + b.size();
        }
    }

    // Has an instance method → not "all static" → rule 1 does not apply,
    // and the name is fine for a regular class.
    public static class Service {

        public String hello() {
            return "hi";
        }

        public static int magic() {
            return 42;
        }
    }

    // All-static but no public method → rule 1 does not apply.
    public static class InternalHelper {

        static int privatelyHelpful() {
            return 1;
        }
    }

    // Abstract class — skipped entirely.
    public abstract static class AbstractBase {

        public static String label() {
            return "x";
        }
    }
}
