package com.example;

public class UnnecessaryLineWrapEdgeCases {

    // Case A: try without resources — should be skipped
    void tryWithoutResources() {
        try {
            throw new Exception();
        } catch (Exception e) {
            // catch
        }
    }

    // Case B: class with long chain (4+ calls) — should be skipped
    void longChain(java.util.List<String> items) {
        items.stream()
            .filter(s -> !s.isEmpty())
            .map(String::trim)
            .sorted()
            .toList();
    }

    // Case C: container type (class) should not count long chain from child
    static class InnerClass extends Thread {
        void containsChain() {
            new StringBuilder().append("a").append("b").append("c").append("d");
        }
    }

    // Case D: if condition already on one line
    void singleLineIf() {
        if (true) { }
    }

    // Case E: method with no type/ident in unusual config
    @Override
    public String toString() {
        return "edge";
    }

    // Case F: record with components on one line
    record SmallRecord(String a, int b) { }

    // Case G: compact ctor on one line
    record AnotherRecord(int x) {
        AnotherRecord { }
    }

    // Case H: interface already on one line
    interface SmallInterface { }

    // Case I: method that wraps but has closing bracket abutting previous token
    void closingBracket(String arg) {
        String val = computeValue(arg);
    }

    private String computeValue(String a) {
        return a;
    }
}
