package com.example;

public class ChainedCallValid {

    // Case 1: Single method call — not a chain
    void singleCall() {
        StringBuilder sb = new StringBuilder();
        sb.append("a");
    }

    // Case 2: Chain already on separate lines — correct
    void multiLineChain() {
        StringBuilder sb = new StringBuilder();
        sb.append("a")
            .append("b")
            .append("c");
    }

    // Case 3: Regular non-chain method call
    void regularCall(String text) {
        String result = text.toUpperCase();
    }

    // Case 4: Single chained call (getter.method) — not a pipeline
    void getterCall(java.util.List<String> list) {
        int size = list.size();
    }

    // Case 5: Two-call chain — allowed (not a pipeline)
    void twoCallChain(String text) {
        String result = text.substring(0).strip();
    }

    // Case 6: Method call followed by getter — common pattern
    void methodThenGetter() {
        String value = "hello".toUpperCase().trim();
    }

    // Case 7: Three-call accessor chain — allowed (under threshold)
    void threeCallAccessor(java.io.File file) {
        String name = file.getName().toLowerCase().trim();
    }
}
