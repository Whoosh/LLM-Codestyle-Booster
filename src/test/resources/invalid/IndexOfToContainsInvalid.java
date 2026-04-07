package test;

public class IndexOfToContainsInvalid {

    public void check(String s, String sub) {
        if (s.indexOf(sub) < 0) {
            System.out.println("not found 1");
        }
        if (s.indexOf(sub) >= 0) {
            System.out.println("found 2");
        }
        if (s.indexOf(sub) == -1) {
            System.out.println("not found 3");
        }
        if (s.indexOf(sub) != -1) {
            System.out.println("found 4");
        }
        if (s.indexOf(sub) > -1) {
            System.out.println("found 5");
        }
        // Reversed forms
        if (0 > s.indexOf(sub)) {
            System.out.println("not found 6 — reversed");
        }
        if (-1 == s.indexOf(sub)) {
            System.out.println("not found 7 — reversed");
        }
        if (-1 != s.indexOf(sub)) {
            System.out.println("found 8 — reversed");
        }
        if (-1 < s.indexOf(sub)) {
            System.out.println("found 9 — reversed");
        }
    }
}
