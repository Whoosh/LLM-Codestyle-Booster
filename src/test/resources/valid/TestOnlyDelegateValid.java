package test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class TestOnlyDelegateValid {

    // === Legitimate delegation patterns ===

    // Case 1: @Override method delegating to private — template method pattern
    @Override
    public String toString() {
        return buildString();
    }

    // Case 2: delegate calls non-private method — not a test-access workaround
    static String publicWrapper(String s) {
        return transform(s);
    }

    // === Multi-statement methods — not thin delegates ===

    // Case 3: two statements — has preprocessing logic
    static String withPreprocess(String text) {
        String cleaned = text.strip();
        return processInternal(cleaned);
    }

    // Case 4: guard clause + delegation
    static String withGuard(String text) {
        if (text == null) {
            return "";
        }
        return processInternal(text);
    }

    // Case 5: try-catch wrapping — adds error handling
    static String withErrorHandling(String text) {
        try {
            return processInternal(text);
        } catch (Exception e) {
            return "error";
        }
    }

    // Case 6: logging before delegation — adds observability
    static String withLogging(String text) {
        System.out.println("processing: " + text);
        return processInternal(text);
    }

    // === Not a method call at all ===

    // Case 7: returns a field — no delegation
    static String getConstant() {
        return CONSTANT;
    }

    // Case 8: returns a literal
    static int getDefault() {
        return 0;
    }

    // Case 9: returns computed expression (not a single method call)
    static int compute(int a, int b) {
        return a + b;
    }

    // === Dot-qualified calls (not same-class private methods) ===

    // Case 10: calls method on parameter object
    static int getSize(List<String> items) {
        return items.size();
    }

    // Case 11: calls static method on another class
    static List<String> wrapImmutable(List<String> items) {
        return Collections.unmodifiableList(items);
    }

    // Case 12: calls this.method() — explicit receiver, different pattern
    String explicitThis() {
        return this.buildString();
    }

    // === Already private — no need to flag ===

    // Case 13: private calling another private
    private static String privateDelegate(String s) {
        return processInternal(s);
    }

    // === Special structures ===

    // Case 14: ternary in return — adds conditional logic
    static String conditional(String text, boolean flag) {
        return flag ? processInternal(text) : text;
    }

    // Case 15: cast in return — changes the return type
    static Object castDelegate(String text) {
        return (Object) processInternal(text);
    }

    // Case 16: abstract method — no body to check
    // (can't be in concrete class, but the check should not crash)

    // Case 17: method calling private method but result is further operated on
    static String chainedDelegate(String text) {
        return processInternal(text).toUpperCase();
    }

    // Case 18: new object construction — not a method call delegate
    static Map<String, String> createMap() {
        return new java.util.LinkedHashMap<>();
    }

    // Case 19: multiple private calls — composition, not thin delegate
    static String compose(String a, String b) {
        return processInternal(a) + processInternal(b);
    }

    // Case 20: lambda/anonymous class — not a thin delegate
    static Runnable createRunner(String text) {
        return () -> processInternal(text);
    }

    // === Helpers ===

    private static final String CONSTANT = "value";

    static String transform(String s) {
        return s.toUpperCase();
    }

    private static String processInternal(String s) {
        return s;
    }

    private String buildString() {
        return "value";
    }
}
