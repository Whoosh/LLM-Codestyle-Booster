package test;

public class UnicodeEscapeInvalid {

    // This comment contains a unicode escape: \u0410 (Cyrillic A)
    private static final String UNICODE_IN_FIELD = "Hello \u0041 world";

    public String badMethod() {
        // \u03C0 should be a real pi character
        return "value";
    }
}
