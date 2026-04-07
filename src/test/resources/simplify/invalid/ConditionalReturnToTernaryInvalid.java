package com.example;
public class ConditionalReturnToTernaryInvalid {
    String status(boolean active) {
        if (active) {
            return "yes";
        } else {
            return "no";
        }
    }
    int value(boolean flag) {
        if (flag) {
            return 1;
        } else {
            return 0;
        }
    }
}
