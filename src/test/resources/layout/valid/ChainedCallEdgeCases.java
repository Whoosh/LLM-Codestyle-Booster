package com.example;

public class ChainedCallEdgeCases {

    // Case: 4-call chain already split across multiple lines — valid
    void multiLineChainLong() {
        new StringBuilder()
            .append("a")
            .append("b")
            .append("c")
            .append("d");
    }

    // Case: inner method call (part of outer chain) — should be skipped
    void nestedChain() {
        new StringBuilder()
            .append("a")
            .append(String.valueOf(42).trim().toLowerCase())
            .append("c")
            .append("d");
    }

    // Case: chain with exactly threshold-1 calls — below threshold
    void belowThreshold() {
        new StringBuilder().append("a").append("b").append("c");
    }

    // Case: no method calls at all
    void noMethodCalls() {
        int x = 1 + 2 + 3;
    }
}
