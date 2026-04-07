package com.example;

public class ConditionalReturnToTernaryValid {

    String status(boolean active) {
        return active ? "yes" : "no";
    }

    String elseIfChain(boolean a, boolean b) {
        if (a) {
            return "a";
        } else if (b) {
            return "b";
        } else {
            return "default";
        }
    }

    int multiStatement(boolean flag) {
        if (flag) {
            log();
            return 1;
        } else {
            return 0;
        }
    }

    private void log() {
    }
}
