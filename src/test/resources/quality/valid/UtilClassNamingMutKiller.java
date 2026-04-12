package quality.valid;

public class UtilClassNamingMutKiller {

    // Non-static nested class with all-static public methods — should NOT be flagged
    // because non-static nested classes can't serve as util classes
    // (they require an outer instance).
    // isNonStaticNestedClass returns true → class is skipped
    // If the !hasModifier(LITERAL_STATIC) part is negated, this class would
    // NOT be treated as non-static nested → would be checked → false positive
    class NonStaticHelper {

        public static String helper() {
            return "x";
        }
    }

    // Static nested class with all-static public methods ending in "Util" — valid
    public static class InnerUtil {

        public static String clean(String s) {
            return s.strip();
        }
    }
}
