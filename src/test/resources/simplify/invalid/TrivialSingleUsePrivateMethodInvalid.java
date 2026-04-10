package simplify.invalid;

import java.util.Set;

public class TrivialSingleUsePrivateMethodInvalid {

    private static final Set<Integer> KNOWN = Set.of(1, 2, 3);

    // Trivial body, called once → flag.
    private static boolean isKnown(int value) {
        return KNOWN.contains(value);
    }

    public boolean checkA(int v) {
        return isKnown(v);
    }

    // Single expression statement, called once → flag.
    private void log(String msg) {
        System.out.println(msg);
    }

    public void emit() {
        log("hello");
    }

    // Single throw statement, called once → flag.
    private void fail(String why) {
        throw new IllegalStateException(why);
    }

    public void use() {
        fail("nope");
    }

    // Trivial body, called once via method reference → flag.
    private boolean accept(String s) {
        return s.startsWith("a");
    }

    public boolean any(java.util.List<String> items) {
        return items.stream().anyMatch(this::accept);
    }

    // Called once from inside a loop → still safe to inline (one source location).
    private static int doubled(int x) {
        return x + 7;
    }

    public int sumOfDoubled(int n) {
        int total = 0;
        for (int i = 0; i < n; i++) {
            total += doubled(i);
        }
        return total;
    }
}
