package io.github.llmcodestyle.quality;

import io.github.llmcodestyle.utils.TestCheckSupportUtil;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class LongTestLiteralCheckTest {

    private static final int EXPECTED_VIOLATIONS = 3;
    private static final String SHORT_MAX = "10";

    @Test
    void invalidCasesProduceViolations() throws Exception {
        List<AuditEvent> violations = TestCheckSupportUtil.runTreeWalkerCheck(LongTestLiteralCheck.class, "quality/invalid/LongTestLiteralInvalid.java", Map.of());
        assertEquals(EXPECTED_VIOLATIONS, violations.size(), "Expected 3 long test literal violations, got " + violations.size());
    }

    @Test
    void validCasesProduceNoViolations() throws Exception {
        assertTrue(TestCheckSupportUtil.runTreeWalkerCheck(LongTestLiteralCheck.class, "quality/valid/LongTestLiteralValid.java", Map.of()).isEmpty(), "Expected no violations");
    }

    @Test
    void setMaxLengthAffectsThreshold() throws Exception {
        new LongTestLiteralCheck().setMaxLength(Integer.parseInt(SHORT_MAX));
        assertFalse(
            TestCheckSupportUtil.runTreeWalkerCheck(
                LongTestLiteralCheck.class,
                "quality/invalid/LongTestLiteralInvalid.java",
                Map.of("maxLength", SHORT_MAX)).isEmpty(),
            "Smaller max length should produce violations");
    }
}
