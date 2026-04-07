package com.example;
import java.util.List;
import java.util.Set;
import java.util.Map;
public class CollectionsToListOfValid {
    List<String> empty() {
        return List.of();
    }
    Set<String> emptySet() {
        return Set.of();
    }
    List<String> single(String s) {
        return List.of(s);
    }
    Map<String, String> emptyMap() {
        return Map.of();
    }
}
