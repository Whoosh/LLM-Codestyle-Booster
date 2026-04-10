package io.github.llmcodestyle.layout;

import io.github.llmcodestyle.utils.TestCheckSupportUtil;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BlankLineAfterCommentCheckTest {

    private static final int EXPECTED_VIOLATIONS = 5;

    @Test
    void invalidCasesProduceViolations() throws Exception {
        List<AuditEvent> violations = TestCheckSupportUtil.runFileSetCheck(
            BlankLineAfterCommentCheck.class,
            "layout/invalid/BlankLineAfterCommentInvalid.java",
            Map.of("fileExtensions", "java"));
        assertEquals(EXPECTED_VIOLATIONS, violations.size(), "Expected 5 violations, got: " + violations.size());
    }

    @Test
    void validCasesProduceNoViolations() throws Exception {
        assertTrue(
            TestCheckSupportUtil.runFileSetCheck(
                BlankLineAfterCommentCheck.class,
                "layout/valid/BlankLineAfterCommentValid.java",
                Map.of("fileExtensions", "java")).isEmpty(),
            "Expected no violations");
    }

    @Test
    void messageIsDescriptive() throws Exception {
        for (AuditEvent event : TestCheckSupportUtil.runFileSetCheck(
            BlankLineAfterCommentCheck.class,
            "layout/invalid/BlankLineAfterCommentInvalid.java",
            Map.of("fileExtensions", "java"))) {
            assertTrue(event.getMessage().contains("blank line"), "Expected 'blank line' in message, got: " + event.getMessage());
        }
    }
}
