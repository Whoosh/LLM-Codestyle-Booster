package test;

import java.util.List;

public class UseIsEmptyValid {

    public void usingIsEmpty(String s, List<String> list) {
        // Correct usage — isEmpty()
        if (s.isEmpty()) {
            System.out.println("empty");
        }
        if (!s.isEmpty()) {
            System.out.println("not empty");
        }
        if (list.isEmpty()) {
            System.out.println("empty list");
        }
        if (!list.isEmpty()) {
            System.out.println("non-empty list");
        }
    }

    public void lengthForOtherComparisons(String s) {
        // Comparing length to values other than 0/1 — valid, not replaceable
        if (s.length() > 5) {
            System.out.println("long");
        }
        if (s.length() == 10) {
            System.out.println("exactly 10");
        }
        if (s.length() < 100) {
            System.out.println("short");
        }
        if (s.length() >= 3) {
            System.out.println("at least 3");
        }
    }

    public void sizeForOtherComparisons(List<String> list) {
        if (list.size() > 5) {
            System.out.println("big list");
        }
        if (list.size() == 3) {
            System.out.println("exactly 3");
        }
    }

    public void lengthWithArgs() {
        // length(int) — not the same as length(), shouldn't match
        // (hypothetical custom method)
        String s = "test";
        int len = s.length();
        if (len > 0) {
            System.out.println("already stored");
        }
    }
}
