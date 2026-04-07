package test;

public class SingleUseVarInvalid {

    // Case 1: classic — variable used once in return
    public boolean validate(String field) {
        String fixed = fixIt(field);
        return fixed.equals(field);
    }

    // Case 2: variable used once in method call
    public void process(String input) {
        String trimmed = input.trim();
        System.out.println(trimmed);
    }

    // Case 3: variable used once in assignment
    public void assign() {
        int computed = calculate();
        int result = computed + 1;
    }

    // Case 4: variable used once in if-condition
    public void check(String s) {
        boolean empty = s.isEmpty();
        if (empty) {
            return;
        }
    }

    // Case 5: variable used once in for-each header (iterable) — safe to inline
    public void forEachHeader(java.util.Map<String, String> map) {
        java.util.Set<java.util.Map.Entry<String, String>> entries = map.entrySet();
        for (java.util.Map.Entry<String, String> entry : entries) {
            System.out.println(entry);
        }
    }

    // Case 6: variable inside while-loop body — used once in next statement
    public void insideWhile(java.util.regex.Matcher m, java.util.List<int[]> result) {
        while (m.find()) {
            int number = Integer.parseInt(m.group(1));
            result.add(new int[]{number, m.end(), m.start()});
        }
    }

    // Case 7: variable inside if-body — used once in next statement
    public void insideIf(String input) {
        if (input != null) {
            String upper = input.toUpperCase();
            System.out.println(upper);
        }
    }

    // Case 8: variable inside try-body — used once in next statement
    public void insideTry() {
        try {
            String value = compute();
            System.out.println(value);
        } catch (Exception ignored) {
        }
    }

    // Case 9: variable inside lambda — used once in next statement
    public void insideLambda() {
        Runnable r = () -> {
            String msg = compute();
            System.out.println(msg);
        };
    }

    private String fixIt(String s) {
        return s;
    }

    private int calculate() {
        return 42;
    }

    private String compute() {
        return "x";
    }
}
