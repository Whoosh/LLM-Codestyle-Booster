package test;

import java.util.List;

public class PureSingleUseVarValid {

    // Case 1: used in next statement — handled by SingleUseLocalVariableCheck, not this one
    void usedInNext(List<String> list) {
        String first = list.get(0);
        System.out.println(first);
    }

    // Case 2: used twice — can't inline
    void usedTwice(int[] arr) {
        int x = arr[0];
        System.out.println("first: " + x);
        System.out.println("again: " + x);
    }

    // Case 3: impure initializer — new object
    void impureNew() {
        Object obj = new Object();
        System.out.println("middle");
        System.out.println(obj);
    }

    // Case 4: used inside loop — would change asymptotic cost
    void usedInLoop(int[] arr, List<String> items) {
        int x = arr[0];
        System.out.println("setup");
        for (String item : items) {
            System.out.println(x + item);
        }
    }

    // Case 5: impure method call in initializer (not in whitelist)
    void impureMethod() {
        String data = readData();
        System.out.println("middle");
        System.out.println(data);
    }

    // Case 6: not used at all — not our concern
    void notUsed(int[] arr) {
        int x = arr[0];
    }

    // Case 7: used inside lambda — repeating context
    void usedInLambda(int[] arr, List<String> items) {
        int x = arr[0];
        System.out.println("setup");
        items.forEach(item -> System.out.println(x + item));
    }

    private String readData() {
        return "data";
    }
}
