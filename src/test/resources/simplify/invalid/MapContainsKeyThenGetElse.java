package com.example;

import java.util.Map;

public class MapContainsKeyThenGetElse {

    // containsKey check with get in else branch — should still be flagged
    String lookupInElse(Map<String, String> map, String key) {
        if (map.containsKey(key)) {
            System.out.println("found");
        } else {
            return "not found";
        }
        return map.get(key);
    }

    // Negated containsKey with get in else — the get is in else block
    String negatedContainsKey(Map<String, String> map, String key) {
        if (!map.containsKey(key)) {
            return "default";
        } else {
            return map.get(key);
        }
    }
}
