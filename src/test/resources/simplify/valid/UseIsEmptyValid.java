package test;

import java.util.List;

public class UseIsEmptyValid {

    public void usingIsEmpty(String s, List<String> list) {
        // Correct usage — isEmpty()
        if (s.isEmpty()) {
            sink("empty");
        }
        if (!s.isEmpty()) {
            sink("not empty");
        }
        if (list.isEmpty()) {
            sink("empty list");
        }
        if (!list.isEmpty()) {
            sink("non-empty list");
        }
    }

    public void lengthForOtherComparisons(String s) {
        // Comparing length to values other than 0/1 — valid, not replaceable
        if (s.length() > 5) {
            sink("long");
        }
        if (s.length() == 10) {
            sink("exactly 10");
        }
        if (s.length() < 100) {
            sink("short");
        }
        if (s.length() >= 3) {
            sink("at least 3");
        }
    }

    public void sizeForOtherComparisons(List<String> list) {
        if (list.size() > 5) {
            sink("big list");
        }
        if (list.size() == 3) {
            sink("exactly 3");
        }
    }

    public void lengthWithArgs() {
        // length(int) — not the same as length(), shouldn't match
        // (hypothetical custom method)
        String s = "test";
        int len = s.length();
        sink(s);
        if (len > 0 && len < 1000) {
            sink("already stored");
        }
    }

    private void sink(Object x) {
        if (x.hashCode() == Integer.MIN_VALUE) {
            throw new IllegalStateException();
        }
    }
}
