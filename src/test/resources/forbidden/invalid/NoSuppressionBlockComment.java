package test;

public class NoSuppressionBlockComment {

    /* NOPMD: justified by review */
    String a = "x";

    /* CHECKSTYLE:OFF */
    String b = "y";

    /*
     * Multi-line block with SUPPRESSFBWARNINGS in the middle.
     */
    String c = "z";

    /* SuppressFBWarnings */
    String d = "w";
}
