package test;

import java.util.List;
import java.util.Map;

public class PureSingleUseVarMutationKiller {

    // Case 1: extractMethodName with DOT (method call like str.length())
    // Exercises extractMethodName line 153: dot != null path
    void dotMethodCall(String input, Map<Integer, String> results) {
        int len = input.length();
        String body = compute();
        if (!body.isEmpty()) {
            results.put(len, body);
        }
    }

    // Case 2: extractMethodName without DOT (bare method call like Integer.parseInt)
    // Exercises extractMethodName line 157: nameIdent != null path
    void bareMethodCall(String text, Map<Integer, String> results) {
        int num = Integer.parseInt(text);
        String body = compute();
        if (!body.isEmpty()) {
            results.put(num, body);
        }
    }

    // Case 3: isEligibleBlock for if-body SLIST (isFlowBlock)
    // Exercises isEligibleBlock line 195 and isFlowBlock line 199
    void varInIfBody(int[] data, Map<Integer, String> results) {
        if (data.length > 0) {
            int val = data[0];
            String body = compute();
            if (!body.isEmpty()) {
                results.put(val, body);
            }
        }
    }

    // Case 4: isEligibleBlock for loop body
    // Exercises isFlowBlock for loop condition type
    void varInForBody(int[] data, Map<Integer, String> results) {
        for (int i = 0; i < data.length; i++) {
            int val = data[i];
            String body = compute();
            results.put(val, body);
        }
    }

    // Case 5: variable in lambda body (METHOD_LIKE_BLOCKS)
    void varInLambda(List<String> items, Map<Integer, String> results) {
        items.forEach(item -> {
            int len = item.length();
            String body = compute();
            results.put(len, body);
        });
    }

    private String compute() {
        return "value";
    }
}
