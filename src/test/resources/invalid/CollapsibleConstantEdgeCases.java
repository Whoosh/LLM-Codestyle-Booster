package test;

public class CollapsibleConstantEdgeCases {

    // Edge 1: nested class has its own constants — should fire independently
    static class Inner {
        static final String X = "inner";
        static final String Y = "const";
        static final String XY = X + Y; // violation — collapsible in Inner scope
    }

    // Edge 2: enum with collapsible constants
    enum Color {
        ;
        static final String R = "red";
        static final String G = "green";
        static final String RG = R + G; // violation
    }

    // Edge 3: interface constants
    interface Config {
        String HOST = "localhost";
        String PORT = "8080";
        // Interface fields are implicitly static final
        String ADDR = HOST + ":" + PORT; // violation — 3 operands
    }

    // Edge 4: record with static constants and method body run
    record Writer(String path) {
        static final String PREFIX = "INSERT";
        static final String TABLE = "tbl";
        static final String COLS = " (a,b)";
        // Field: 2 collapsible operands → violation
        static final String SQL = PREFIX + TABLE;
        // Method body: run of 3 constants + literal → violation
        String build() {
            return PREFIX + TABLE + COLS + "val" + path;
        }
    }
}
