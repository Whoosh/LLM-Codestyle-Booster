package io.github.llmcodestyle.forbidden;

import com.puppycrawl.tools.checkstyle.api.AbstractFileSetCheck;
import com.puppycrawl.tools.checkstyle.api.FileText;

import java.io.File;

/** Forbids backslash-u Unicode escape sequences in Java source. Use real UTF-8 characters directly; control characters are exempt. */
public class UnicodeEscapeCheck extends AbstractFileSetCheck {

    /** Violation message key. */
    static final String MSG_KEY = "unicode.escape.forbidden";

    private static final int CONTROL_CHAR_MAX = 0x1F;
    private static final int DEL_CHAR = 0x7F;
    private static final int ESCAPE_PREFIX_LEN = 2;
    private static final int HEX_DIGITS_LEN = 4;
    private static final int HEX_RADIX = 16;

    @Override
    protected void processFiltered(File file, FileText fileText) {
        for (int lineNo = 0; lineNo < fileText.size(); lineNo++) {
            checkLineForUnicodeEscape(fileText.get(lineNo), lineNo + 1);
        }
    }

    private void checkLineForUnicodeEscape(String line, int lineNo) {
        int idx = 0;
        while (idx < line.length()) {
            int escapeIdx = findEscapeU(line, idx);
            if (escapeIdx < 0) {
                break;
            }
            int hexStart = escapeIdx + ESCAPE_PREFIX_LEN;
            if (hexStart + HEX_DIGITS_LEN > line.length()) {
                idx = escapeIdx + 1;
                continue;
            }
            String hex = line.substring(hexStart, hexStart + HEX_DIGITS_LEN);
            if (isHexDigits(hex)) {
                if (!isExempt(Integer.parseInt(hex, HEX_RADIX))) {
                    log(lineNo, escapeIdx, MSG_KEY, hex);
                }
                idx = hexStart + HEX_DIGITS_LEN;
            } else {
                idx = escapeIdx + 1;
            }
        }
    }

    private static int findEscapeU(String line, int fromIndex) {
        for (int i = fromIndex; i < line.length() - 1; i++) {
            if (line.charAt(i) == '\\' && line.charAt(i + 1) == 'u') {
                return i;
            }
        }
        return -1;
    }

    private static boolean isHexDigits(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (!isHexDigit(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private static boolean isHexDigit(char c) {
        return c >= '0' && c <= '9' || c >= 'a' && c <= 'f' || c >= 'A' && c <= 'F';
    }

    private static boolean isExempt(int codePoint) {
        return codePoint <= CONTROL_CHAR_MAX || codePoint == DEL_CHAR;
    }
}
