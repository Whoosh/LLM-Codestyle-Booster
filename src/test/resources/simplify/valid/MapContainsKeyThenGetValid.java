package com.example;
import java.util.Map;
public class MapContainsKeyThenGetValid {
    String lookup(Map<String, String> map, String key) {
        return map.getOrDefault(key, "default");
    }
    String lookupDirect(Map<String, String> map, String key) {
        if (map.containsKey(key)) {
            sink("found");
        }
        return "";
    }
    private void sink(Object x) {
        if (x.hashCode() == Integer.MIN_VALUE) {
            throw new IllegalStateException();
        }
    }
}
