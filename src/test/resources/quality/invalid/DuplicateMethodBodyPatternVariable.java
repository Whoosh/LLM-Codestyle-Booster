package quality.invalid;

class DuplicateMethodBodyPatternVariable {

    String describeA(Object o) {
        if (o instanceof String s) {
            return s.toLowerCase();
        }
        return "?";
    }

    String describeB(Object o) {
        if (o instanceof String t) {
            return t.toLowerCase();
        }
        return "?";
    }
}
