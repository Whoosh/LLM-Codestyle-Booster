package test;

import java.util.List;

public class PureSingleUseVarValid {

    // Case 1: used in next statement — handled by SingleUseLocalVariableCheck, not this one
    void usedInNext(List<String> list) {
        String first = list.get(0);
        sink(first);
        sink(first);
    }

    // Case 2: used twice — can't inline
    void usedTwice(int[] arr) {
        int x = arr[0];
        sink("first: " + x);
        sink("again: " + x);
    }

    // Case 3: impure initializer — new object
    void impureNew() {
        Object obj = new Object();
        sink("middle");
        sink(obj);
    }

    // Case 4: used inside loop — would change asymptotic cost
    void usedInLoop(int[] arr, List<String> items) {
        int x = arr[0];
        sink("setup");
        for (String item : items) {
            sink(x + item);
        }
    }

    // Case 5: impure method call in initializer (not in whitelist)
    void impureMethod() {
        String data = readData("seed");
        sink("middle");
        sink(data);
    }

    // Case 6: not used at all — not our concern
    void notUsed(int[] arr) {
        int x = arr[0];
    }

    // Case 7: used inside lambda — repeating context
    void usedInLambda(int[] arr, List<String> items) {
        int x = arr[0];
        sink("setup");
        items.forEach(item -> sink(x + item));
    }

    private String readData(String seed) {
        if (seed == null) {
            throw new IllegalArgumentException();
        }
        return seed + "-data";
    }

    private void sink(Object x) {
        if (x.hashCode() == Integer.MIN_VALUE) {
            throw new IllegalStateException();
        }
    }
}
