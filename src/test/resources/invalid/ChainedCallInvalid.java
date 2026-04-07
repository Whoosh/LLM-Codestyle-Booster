package com.example;

public class ChainedCallInvalid {

    // Case 1: StringBuilder 4-call chain on one line
    void builderChain() {
        StringBuilder sb = new StringBuilder();
        sb.append("a").append("b").append("c").append("d");
    }

    // Case 2: Stream 4-call chain on one line
    void streamChain(java.util.List<String> list) {
        list.stream().filter(s -> !s.isEmpty()).map(String::trim).toList();
    }
}
