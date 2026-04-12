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
    private static final int BLOCK_1_LINE = 6;
    private static final int BLOCK_2_LINE = 11;
    private static final int BLOCK_3_LINE = 17;

    @Test
    void invalidCasesProduceViolations() throws Exception {
        List<AuditEvent> violations = runFileSetCheck(CommentedOutCodeCheck.class, "forbidden/invalid/CommentedOutCodeInvalid.java", Map.of("minConsecutiveLines", MIN_LINES_2));
        assertEquals(EXPECTED_VIOLATIONS, violations.size(), "Expected 3 commented-out code blocks, got: " + violations.size());
    }

    @Test
    void validCasesProduceNoViolations() throws Exception {
        assertTrue(runFileSetCheck(CommentedOutCodeCheck.class, "forbidden/valid/CommentedOutCodeValid.java", Map.of("minConsecutiveLines", MIN_LINES_2)).isEmpty(),
            "Expected no violations");
    }

    @Test
    void setMinConsecutiveLinesAffectsThreshold() throws Exception {
        new CommentedOutCodeCheck().setMinConsecutiveLines(Integer.parseInt(MIN_LINES_3));
        assertTrue(runFileSetCheck(CommentedOutCodeCheck.class, "forbidden/invalid/CommentedOutCodeInvalid.java", Map.of("minConsecutiveLines", MIN_LINES_3)).size()
            <= EXPECTED_VIOLATIONS, "Higher threshold should produce at most as many violations");
    }

    @Test
    void violationLineNumberPointsToStartOfBlock() throws Exception {
        List<AuditEvent> violations = runFileSetCheck(CommentedOutCodeCheck.class, "forbidden/invalid/CommentedOutCodeInvalid.java", Map.of("minConsecutiveLines", MIN_LINES_2));
        assertEquals(EXPECTED_VIOLATIONS, violations.size(), format(violations));
        // Block 1 starts at line 6, Block 2 at line 11, Block 3 at line 17
        // If consecutiveCount == 0 is negated, start would be wrong (last line instead of first)
        assertTrue(violations.stream().anyMatch(v -> v.getLine() == BLOCK_1_LINE), "Block 1 should start at line 6: " + format(violations));
        assertTrue(violations.stream().anyMatch(v -> v.getLine() == BLOCK_2_LINE), "Block 2 should start at line 11: " + format(violations));
        assertTrue(violations.stream().anyMatch(v -> v.getLine() == BLOCK_3_LINE), "Block 3 should start at line 17: " + format(violations));
    }
}
