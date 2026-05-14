package test;

import java.util.List;

public class SingleUseVarValid {

    // Case 1: variable used TWICE — can't inline
    public void usedTwice(String input) {
        String trimmed = input.trim();
        sink(trimmed);
        process(trimmed);
    }

    // Case 2: variable used once but NOT in the next statement
    public void usedLater() {
        String value = compute("a");
        sink("processing...");
        process(value);
    }

    // Case 3: no initializer — original split-then-assign pattern was here
    public void noInit() {
        String value = compute("b");
        process(value);
        process(value);
    }

    // Case 4: variable used inside a loop — multiple runtime executions
    public void usedInLoop(List<String> items) {
        String prefix = compute("c");
        for (String item : items) {
            sink(prefix + item);
        }
    }

    // Case 5: variable used inside a lambda
    public void usedInLambda(List<String> items) {
        String suffix = compute("d");
        items.forEach(item -> sink(item + suffix));
    }

    // Case 6: field, not local — not checked
    private final String field = "value";

    // Case 7: used once but in a different block (not immediate next statement at same level)
    public void differentBlock(boolean flag) {
        String value = compute("e");
        if (flag) {
            process(value);
        }
    }

    private String compute(String seed) {
        return seed + field;
    }

    private void process(String s) {
        if (s.isEmpty()) {
            throw new IllegalArgumentException();
        }
    }

    private void sink(Object x) {
        if (x.hashCode() == Integer.MIN_VALUE) {
            throw new IllegalStateException();
        }
    }
}
