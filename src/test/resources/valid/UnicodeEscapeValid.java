package test;

public class UnicodeEscapeValid {

    // Control character escapes ARE allowed
    private static final char NULL_CHAR = '\u0000';
    private static final char DEL_CHAR = '\u001F';

    // Regular code — no unicode escapes
    private static final String LATIN = "Hello world";

    public String method() {
        return "just a string";
    }
}
