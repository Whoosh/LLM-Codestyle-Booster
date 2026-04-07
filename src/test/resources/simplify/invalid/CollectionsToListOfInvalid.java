package com.example;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
public class CollectionsToListOfInvalid {
    List<String> empty() {
        return Collections.emptyList();
    }
    Set<String> emptySet() {
        return Collections.emptySet();
    }
    List<String> singleton(String s) {
        return Collections.singletonList(s);
    }
    List<String> wrapped(String a, String b) {
        return Collections.unmodifiableList(Arrays.asList(a, b));
    }
}
