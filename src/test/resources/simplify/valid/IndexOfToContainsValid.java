package test;

public class IndexOfToContainsValid {

    public void check(String s, String sub) {
        // Using contains directly — valid
        if (!s.contains(sub)) {
            System.out.println("not found");
        }
        if (s.contains(sub)) {
            System.out.println("found");
        }
        // indexOf with fromIndex — NOT flagged (two arguments)
        int idx = s.indexOf(sub, 5);
        // Storing result for later use — NOT flagged
        int pos = s.indexOf(sub);
        if (pos > 2) {
            System.out.println("found after pos 2");
        }
        // 0 < indexOf — means "found after position 0", NOT equivalent to contains
        if (0 < s.indexOf(sub)) {
            System.out.println("found after start");
        }
    }
}
