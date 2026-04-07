package test;

import java.util.List;

public class SingleUseVarValid {

    // Case 1: variable used TWICE — can't inline
    public void usedTwice(String input) {
        String trimmed = input.trim();
        System.out.println(trimmed);
        process(trimmed);
    }

    // Case 2: variable used once but NOT in the next statement
    public void usedLater() {
        String value = compute();
        System.out.println("processing...");
        process(value);
    }

    // Case 3: no initializer
    public void noInit() {
        String value;
        value = compute();
        process(value);
    }

    // Case 4: variable used inside a loop — multiple runtime executions
    public void usedInLoop(List<String> items) {
        String prefix = compute();
        for (String item : items) {
            System.out.println(prefix + item);
        }
    }

    // Case 5: variable used inside a lambda
    public void usedInLambda(List<String> items) {
        String suffix = compute();
        items.forEach(item -> System.out.println(item + suffix));
    }

    // Case 6: field, not local — not checked
    private final String field = "value";

    // Case 7: used once but in a different block (not immediate next statement at same level)
    public void differentBlock(boolean flag) {
        String value = compute();
        if (flag) {
            process(value);
        }
    }

    private String compute() {
        return "x";
    }

    private void process(String s) {
    }
}
