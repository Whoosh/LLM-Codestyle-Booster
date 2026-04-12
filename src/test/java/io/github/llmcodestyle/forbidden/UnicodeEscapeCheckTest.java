package io.github.llmcodestyle.forbidden;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static io.github.llmcodestyle.utils.TestCheckSupportUtil.*;
import static org.junit.jupiter.api.Assertions.*;

class UnicodeEscapeCheckTest {

    private static final int EXPECTED_VIOLATIONS = 3;
    private static final Map<String, String> NO_PROPS = Map.of();
    private static final int HEX_VIOLATIONS = 4;
    private static final Pattern HEX_CODE_PATTERN = Pattern.compile(".*[0-9A-Fa-f]{4}.*");

    @Test
    void invalidCasesProduceViolations() throws Exception {
        List<AuditEvent> violations = runFileSetCheck(UnicodeEscapeCheck.class, "forbidden/invalid/UnicodeEscapeInvalid.java", NO_PROPS);
        assertEquals(EXPECTED_VIOLATIONS, violations.size(), "Expected 3 unicode escape violations, got: " + format(violations));
    }

    @Test
    void validCasesProduceNoViolations() throws Exception {
        assertTrue(runFileSetCheck(UnicodeEscapeCheck.class, "forbidden/valid/UnicodeEscapeValid.java", NO_PROPS).isEmpty(), "Expected no violations");
    }

    @Test
    void controlCharacterEscapesAreExempt() throws Exception {
        List<AuditEvent> violations = runFileSetCheck(UnicodeEscapeCheck.class, "forbidden/valid/UnicodeEscapeEdgeCases.java", NO_PROPS);
        assertTrue(violations.isEmpty(), "Control character escapes should be exempt: " + format(violations));
    }

    @Test
    void violationMessageContainsHexCode() throws Exception {
        for (AuditEvent event : runFileSetCheck(UnicodeEscapeCheck.class, "forbidden/invalid/UnicodeEscapeInvalid.java", NO_PROPS)) {
            assertTrue(HEX_CODE_PATTERN.matcher(event.getMessage()).matches(), "Message should contain 4-digit hex code: " + event.getMessage());
        }
    }

    @Test
    void truncatedEscapeAndNonHexAreNotFlagged() throws Exception {
        List<AuditEvent> violations = runFileSetCheck(UnicodeEscapeCheck.class, "forbidden/valid/UnicodeEscapeEdgeCases.java", NO_PROPS);
        assertTrue(violations.isEmpty(), "Truncated escapes and non-hex should not be flagged: " + format(violations));
    }

    @Test
    void uppercaseHexDigitsAreRecognized() throws Exception {
        List<AuditEvent> violations = runFileSetCheck(UnicodeEscapeCheck.class, "forbidden/invalid/UnicodeEscapeUpperHex.java", NO_PROPS);
        assertEquals(HEX_VIOLATIONS, violations.size(), "Uppercase hex digit escapes should be recognized: " + format(violations));
    }

    @Test
    void isHexDigitAllRanges() throws Exception {
        // L74 NO_COVERAGE: `c >= '0' && c <= '9' || c >= 'a' && c <= 'f' || c >= 'A' && c <= 'F'`
        // Exercises all three hex digit ranges plus non-hex characters.
        List<AuditEvent> violations = runFileSetCheck(UnicodeEscapeCheck.class, "forbidden/invalid/UnicodeEscapeHexEdge.java", NO_PROPS);
        // Each of the 4 unicode escapes in strings should be flagged (all are printable, non-control)
        assertEquals(HEX_VIOLATIONS, violations.size(), "All hex ranges (0-9, a-f, A-F) should be recognized: " + format(violations));
    }

    @Test
    void nonHexCharsAreNotRecognized() throws Exception {
        // File contains backslash-u-ZZZZ (non-hex) and truncated sequences
        List<AuditEvent> violations = runFileSetCheck(UnicodeEscapeCheck.class, "forbidden/valid/UnicodeEscapeNonHex.java", NO_PROPS);
        assertTrue(violations.isEmpty(), "Non-hex chars after backslash-u should not be flagged: " + format(violations));
    }
}
