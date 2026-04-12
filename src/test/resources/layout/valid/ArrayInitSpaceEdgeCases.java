package com.example;

public class ArrayInitSpaceEdgeCases {

    // Standalone array init (col 0, no preceding bracket)
    int[] standalone = {1, 2, 3};

    // Array init at start of line (column 0)
    void method() {
        int[] x = new int[]
            {4, 5, 6};
    }
}
