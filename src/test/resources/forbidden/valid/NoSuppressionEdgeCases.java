package test;

public class NoSuppressionEdgeCases {

    // Case: suppression keywords inside double-quoted strings
    String s1 = "// NOPMD inside string literal";
    String s2 = "// CHECKSTYLE:OFF in string";
    String s3 = "// SuppressFBWarnings in string";

    // Case: suppression keywords inside char literals followed by slash
    char c1 = '/';

    // Case: escaped quote inside string followed by suppression-like text
    String s4 = "escaped \" quote // NOPMD still in string";

    // Case: escaped backslash inside string
    String s5 = "path\\\\here // NOPMD still in string";

    // Case: single-quoted char with backslash
    char c2 = '\\';

    // Case: char literal containing double quote
    char c3 = '"';

    // Case: empty line
    // Case: line with only spaces

    void method() {
        // Regular comment without suppression keywords
        String x = "value";
    }
}
