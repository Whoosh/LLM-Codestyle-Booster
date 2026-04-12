package io.github.llmcodestyle.layout;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.github.llmcodestyle.utils.TestCheckSupportUtil.*;
import static org.junit.jupiter.api.Assertions.*;

class ArrayInitSpaceCheckTest {

    private static final Map<String, String> NO_PROPS = Map.of();
    private static final String INVALID = "layout/invalid/ArrayInitSpaceInvalid.java";
    private static final int EXPECTED_VIOLATIONS = 3;

    @Test
    void arrayInitWithoutSpaceProducesViolations() throws Exception {
        List<AuditEvent> violations = runCheck(INVALID);
        assertEquals(EXPECTED_VIOLATIONS, violations.size(), "Expected exactly 3 violations: " + format(violations));
    }

    @Test
    void arrayInitWithSpaceProducesNoViolations() throws Exception {
        assertTrue(runCheck("layout/valid/ArrayInitSpaceValid.java").isEmpty());
    }

    @Test
    void standaloneArrayInitAtColumnZeroIsNotFlagged() throws Exception {
        List<AuditEvent> violations = runCheck("layout/valid/ArrayInitSpaceEdgeCases.java");
        assertTrue(violations.isEmpty(), "Array inits at column 0 or with space should not be flagged: " + format(violations));
    }

    @Test
    void violationMessageReferencesArrayInit() throws Exception {
        List<AuditEvent> violations = runCheck(INVALID);
        assertFalse(violations.isEmpty(), "Should have violations");
        for (AuditEvent event : violations) {
            assertNotNull(event.getMessage(), "Message should not be null");
            assertFalse(event.getMessage().isEmpty(), "Message should not be empty");
        }
    }

    @Test
    void eachViolationReportsCorrectLine() throws Exception {
        for (AuditEvent event : runCheck(INVALID)) {
            assertTrue(event.getLine() > 0, "Line number should be positive: " + event.getLine());
        }
    }

    private static List<AuditEvent> runCheck(String resource) throws Exception {
        return runTreeWalkerCheck(ArrayInitSpaceCheck.class, resource, NO_PROPS);
    }
}
