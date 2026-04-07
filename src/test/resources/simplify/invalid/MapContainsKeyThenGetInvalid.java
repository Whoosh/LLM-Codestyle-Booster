package com.example;
import java.util.Map;
import java.util.HashMap;
public class MapContainsKeyThenGetInvalid {
    String lookup(Map<String, String> map, String key) {
        if (map.containsKey(key)) {
            return map.get(key);
        }
        return "default";
    }
    String lookupWithElse(Map<String, String> map, String key) {
        if (map.containsKey(key)) {
            return map.get(key);
        } else {
            return "missing";
        }
    }
}
