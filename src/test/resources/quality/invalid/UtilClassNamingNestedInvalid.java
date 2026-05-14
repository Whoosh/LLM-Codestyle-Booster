package quality.invalid;

// Outer non-util-shaped class (has an instance method → not all-static, name is fine).
// Contains a nested static class that LOOKS like a util class (all-static, public,
// non-Util suffix) and MUST be flagged independently.
//
// This fixture guards against the bug where shared method-count state on the check
// instance leaked between sibling/nested CLASS_DEF visits, causing the outer's
// counts to be overwritten by the inner before the outer's evaluation completed.
public class UtilClassNamingNestedInvalid {

    // Outer is a regular service: has instance state and an instance method.
    // total=2, static=1, public=2 → NOT util-shaped → NOT flagged.
    private final String prefix = "p";

    public String greet(String name) {
        return prefix + name;
    }

    public static int compute() {
        return 1;
    }

    // Inner static class shaped like a util but named "StringHelper".
    // total=2, static=2, public=2 → util-shaped, name does not end in Util/Utils
    // → MUST be flagged.
    public static class StringHelper {

        public static String trim(String s) {
            return s.strip();
        }

        public static String upper(String s) {
            return s.toUpperCase();
        }
    }
}
