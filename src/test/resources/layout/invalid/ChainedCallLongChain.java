package com.example;

public class ChainedCallLongChain {

    // 8-call chain on one line — should produce exactly 1 violation (the outermost call)
    // If isPartOfOuterChain is broken, inner sub-chains of 4+ calls would also fire
    void longChain() {
        new StringBuilder().append("a").append("b").append("c").append("d").append("e").append("f").append("g").append("h");
    }
}
