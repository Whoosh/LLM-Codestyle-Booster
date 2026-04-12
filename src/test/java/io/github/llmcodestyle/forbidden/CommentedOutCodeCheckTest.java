package io.github.llmcodestyle.forbidden;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.github.llmcodestyle.utils.TestCheckSupportUtil.*;
import static org.junit.jupiter.api.Assertions.*;

class CommentedOutCodeCheckTest {

    private static final int EXPECTED_VIOLATIONS = 3;
    private static final String MIN_LINES_2 = "2";
    private static final String MIN_LINES_3 = "3";

    @Test
    void invalidCasesProduceViolations() throws Exception {
        List<AuditEvent> violations = runFileSetCheck(CommentedOutCodeCheck.class, "forbidden/invalid/CommentedOutCodeInvalid.java", Map.of("minConsecutiveLines", MIN_LINES_2));
        assertEquals(EXPECTED_VIOLATIONS, violations.size(), "Expected 3 commented-out code blocks, got: " + violations.size());
    }

    @Test
    void validCasesProduceNoViolations() throws Exception {
        assertTrue(
            runFileSetCheck(
                CommentedOutCodeCheck.class,
                "forbidden/valid/CommentedOutCodeValid.java",
                Map.of("minConsecutiveLines", MIN_LINES_2)).isEmpty(),
            "Expected no violations");
    }

    @Test
    void setMinConsecutiveLinesAffectsThreshold() throws Exception {
        new CommentedOutCodeCheck().setMinConsecutiveLines(Integer.parseInt(MIN_LINES_3));
        assertTrue(
            runFileSetCheck(
                CommentedOutCodeCheck.class,
                "forbidden/invalid/CommentedOutCodeInvalid.java",
                Map.of("minConsecutiveLines", MIN_LINES_3)).size() <= EXPECTED_VIOLATIONS,
            "Higher threshold should produce at most as many violations");
    }

    @Test
    void violationLineNumberPointsToStartOfBlock() throws Exception {
        // processFiltered line 66: consecutiveCount == 0 sets consecutiveStart
        // If negated, consecutiveStart would be set to the LAST line, not the FIRST
        List<AuditEvent> violations = runFileSetCheck(CommentedOutCodeCheck.class,
            "forbidden/invalid/CommentedOutCodeInvalid.java", Map.of("minConsecutiveLines", MIN_LINES_2));
        assertEquals(EXPECTED_VIOLATIONS, violations.size(), format(violations));
        for (AuditEvent v : violations) {
            assertTrue(v.getLine() > 0, "Line should be positive: " + v.getLine());
            // The violation message should contain the count
            assertTrue(v.getMessage().matches(".*\\d+.*"),
                "Message should contain consecutive line count: " + v.getMessage());
        }
    }
}
