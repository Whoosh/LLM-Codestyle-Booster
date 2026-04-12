package test;

public class NoSuppressionStringEscape {

    // Escaped double quote inside string — should not confuse parser
    String escaped = "escaped \" quote";
    // NOPMD — this IS a suppression comment after the string line

    // Single-quoted char with single quote — tricky parsing
    char singleQuote = '\'';
    // CHECKSTYLE:OFF — another real suppression

    // Backslash in string
    String backslash = "back\\slash";
    // SUPPRESSFBWARNINGS — yet another real suppression
}
