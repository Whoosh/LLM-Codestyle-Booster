package test;

public class NoSuppressionCharAndStringEdge {

    // Line with char literal containing backslash followed by real comment
    char ch = '\\'; // NOPMD

    // Line with double-quote inside string followed by real comment
    String dq = "hello\"world"; // CHECKSTYLE:OFF

    // Line with single-quote inside non-string context followed by real comment
    char sq = '\''; // SUPPRESSFBWARNINGS

    // Line with string then char then comment
    String mixed = "abc"; char x = 'z'; // NOPMD

    // Multi-char string with embedded escape sequences
    String multi = "a\\\"b"; // NOPMD
}
