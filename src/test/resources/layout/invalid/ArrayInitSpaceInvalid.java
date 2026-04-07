package com.example;

public class ArrayInitSpaceInvalid {

    private static final int[] NUMS = new int[]{1, 2, 3};
    private static final String[] NAMES = new String[]{"a", "b"};

    int[] getValues() {
        return new int[]{10, 20};
    }
}
