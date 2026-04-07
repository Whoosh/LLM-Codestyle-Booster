package test;

import java.util.List;
import java.util.Map;

public class PureSingleUseVarInvalid {

    // Case 1: array access used once inside if body
    void arrayAccessInIf(int[][] data, Map<Integer, String> map) {
        int number = data[0][1];
        String body = compute();
        if (!body.isEmpty()) {
            map.put(number, body);
        }
    }

    // Case 2: .get() result used once inside if body
    void getResultInIf(List<int[]> starts, Map<Integer, String> problems) {
        int number = starts.get(0)[0];
        String body = compute();
        if (!body.isEmpty()) {
            problems.putIfAbsent(number, body);
        }
    }

    // Case 3: .length() used two statements later
    void lengthUsedLater(String input) {
        int len = input.length();
        System.out.println("processing");
        System.out.println(len);
    }

    // Case 4: Integer.parseInt used inside if
    void parseIntInIf(String text, Map<Integer, String> results) {
        int num = Integer.parseInt(text);
        String value = compute();
        if (!value.isEmpty()) {
            results.put(num, value);
        }
    }

    private String compute() {
        return "value";
    }
}
