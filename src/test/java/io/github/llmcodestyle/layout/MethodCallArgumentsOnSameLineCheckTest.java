package io.github.llmcodestyle.layout;

import io.github.llmcodestyle.utils.TestCheckSupportUtil;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MethodCallArgumentsOnSameLineCheckTest {

    private static final int EXPECTED_VIOLATIONS = 2;

    @Test
    void invalidCasesProduceViolations() throws Exception {
        List<AuditEvent> violations = TestCheckSupportUtil.runTreeWalkerCheck(MethodCallArgumentsOnSameLineCheck.class, "layout/invalid/MethodCallArgsInvalid.java", Map.of());
        assertEquals(EXPECTED_VIOLATIONS, violations.size(), "Expected 2 mixed-argument violations, got: " + violations.size());
    }

    @Test
    void validCasesProduceNoViolations() throws Exception {
        assertTrue(
            TestCheckSupportUtil.runTreeWalkerCheck(
                MethodCallArgumentsOnSameLineCheck.class, "layout/valid/MethodCallArgsValid.java", Map.of()).isEmpty(),
            "Expected no violations");
    }
}
