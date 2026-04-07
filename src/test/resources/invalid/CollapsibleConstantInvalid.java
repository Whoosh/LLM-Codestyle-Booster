package test;

public class CollapsibleConstantInvalid {

    // Case 1: two string literals concatenated directly
    static final String DIRECT_CONCAT = "Hello" + " World";

    // Case 2: three string literals concatenated
    static final String TRIPLE_CONCAT = "a" + "b" + "c";

    // Case 3: constant references that are both single-literal
    static final String PREFIX = "api";
    static final String SEPARATOR = "/";
    static final String PATH = PREFIX + SEPARATOR;

    // Case 4: mixed — literal + constant reference
    static final String BASE = "http://";
    static final String URL = BASE + "localhost";

    // Case 5: constant + literal reversed
    static final String HOST = "example.com";
    static final String FULL_URL = "https://" + HOST;

    // Case 6: numeric addition of literals
    static final int TOTAL = 10 + 20;

    // Case 7: numeric constant references
    static final int WIDTH = 100;
    static final int HEIGHT = 200;
    static final int AREA = WIDTH + HEIGHT;

    // Case 8: long literals
    static final long BIG_TOTAL = 1000L + 2000L;

    // Case 9: three constants chained
    static final String A = "x";
    static final String B = "y";
    static final String C = "z";
    static final String ABC = A + B + C;

    // Case 10: char literals
    static final String CHAR_CONCAT = "prefix" + "_";

    // Case 11: array element with literal concatenation (like unicode escapes)
    private static final String[] BANNED = {
        "clean value",
        "\\" + "u2714",
        "\\" + "u2718",
    };

    // Case 12: array element with 3 literal parts
    private static final String[] PATHS = {
        "a" + "/" + "b",
        "single",
    };

    // Case 13: array element referencing same-class literal constants
    private static final String[] COMBO = {
        PREFIX + SEPARATOR,
    };

    // Case 14: long chain of constants and literals forming SQL prefix (refactoring artifact)
    static final String INSERT_PREFIX = "INSERT INTO ";
    static final String DEFAULT_TABLE = "example.tbl_data";
    static final String VALUES_COLS = " (col1, col2) VALUES ";
    static final String COL_SEPARATOR = "', '";
    static final String SQL_PREFIX = INSERT_PREFIX + DEFAULT_TABLE + VALUES_COLS + "(''" + COL_SEPARATOR;

    // Case 15: method body with run of 5 consecutive constants/literals before dynamic part
    static String methodWithCollapsibleRun() {
        return INSERT_PREFIX + DEFAULT_TABLE + VALUES_COLS + "(''" + COL_SEPARATOR + dynamicValue();
    }

    // Case 16: method body with run of 3 constants at the end
    static String methodWithTrailingRun() {
        return dynamicValue() + INSERT_PREFIX + DEFAULT_TABLE + VALUES_COLS;
    }

    // Case 17: method body with run of exactly 2 constants (now caught with threshold 2)
    static String methodWithPairRun() {
        return INSERT_PREFIX + DEFAULT_TABLE + dynamicValue();
    }

    private static String dynamicValue() {
        return "dynamic";
    }
}
