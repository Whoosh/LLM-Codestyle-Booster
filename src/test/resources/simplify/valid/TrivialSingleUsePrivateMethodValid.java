package simplify.valid;

import java.util.Set;

public class TrivialSingleUsePrivateMethodValid {

    private static final Set<Integer> KNOWN = Set.of(1, 2, 3);

    // Called from TWO sites — keep as helper.
    private static boolean isKnown(int value) {
        return KNOWN.contains(value);
    }

    public boolean a(int v) {
        return isKnown(v);
    }

    public boolean b(int v) {
        return isKnown(v + 1);
    }

    // Parameter used twice — inlining would duplicate the argument expression.
    private static int squareDup(int x) {
        return x * x;
    }

    public int useSquareDup(int seed) {
        return squareDup(seed);
    }

    // Body has more than one statement — not trivial.
    private static int compute(int n) {
        int sum = 0;
        for (int i = 0; i < n; i++) {
            sum += i;
        }
        return sum;
    }

    public int call() {
        return compute(10);
    }

    // Method has an annotation that must be preserved — never inline.
    @Deprecated
    private static String name() {
        return "old";
    }

    public String getName() {
        return name();
    }

    // Overloaded private methods — names collide, do not flag either.
    private static int convert(int x) {
        return x * 2;
    }

    private static long convert(long x) {
        return x * 2L;
    }

    public int useInt() {
        return convert(5);
    }

    public long useLong() {
        return convert(5L);
    }

    // Generic private method — type witnesses needed at call site, skip.
    private static <T> T identity(T value) {
        return value;
    }

    public String passthrough(String s) {
        return identity(s);
    }

    // Recursive: only one external use, but the method calls itself.
    // External refs = total - self refs = 1 - 1 = 0, so it should NOT be flagged.
    private static int factorial(int n) {
        return n <= 1 ? 1 : n * factorial(n - 1);
    }

    public int useFact() {
        return factorial(5);
    }

    // Body has more than three method calls (stream chain) — earned its name, do not flag.
    private String findFirstStarting(java.util.List<String> items, String prefix) {
        return items.stream().filter(s -> s.startsWith(prefix)).findFirst().orElse("");
    }

    public String useChain(java.util.List<String> items) {
        return findFirstStarting(items, "x");
    }

    // Method called from a nested type counts toward usages.
    private int innerHelper() {
        return 1;
    }

    public int useViaCaller() {
        return innerHelper();
    }

    public int useViaNested() {
        return new java.util.concurrent.Callable<Integer>() {
            @Override
            public Integer call() {
                return innerHelper() + 1;
            }
        }.toString().length();
    }
}
