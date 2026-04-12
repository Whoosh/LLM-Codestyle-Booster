package test;

public class UnicodeEscapeEdgeCases {

    // Exempt: control character \u0000 (NULL)
    char nul = '\u0000';

    // Exempt: control character \u001F (last control char)
    char lastCtrl = '\u001F';

    // Exempt: DEL character \u007F
    char del = '\u007F';

    // Not a unicode escape: \u followed by non-hex
    String notEscape = "\\uXYZZ";

    // Truncated unicode escape at end of line: \u00
    String truncated = "\\u00";

    // No unicode escapes at all
    String clean = "Hello World";

    // Just a backslash not followed by u
    String backslash = "path\\to\\file";
}
