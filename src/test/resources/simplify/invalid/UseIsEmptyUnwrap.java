package com.example;

import java.util.List;

// Tests unwrap and methodName paths in UseIsEmptyCheck
@SuppressWarnings("unused")
public class UseIsEmptyUnwrap {
    // Comparison where left side needs unwrapping (EXPR wrapper around size() call)
    void exprWrappedSize(List<String> list) {
        // size() > 0 -> should suggest isEmpty
        if (list.size() > 0) {
            System.out.println("not empty");
        }
        // 0 < size() -> reversed comparison
        if (0 < list.size()) {
            System.out.println("not empty reverse");
        }
    }
}
