package io.github.llmcodestyle.simplify;

import io.github.llmcodestyle.utils.TestCheckSupportUtil;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class InlineRegexConstantCheckTest {

    private static final int EXPECTED_VIOLATIONS = 5;

    @Test
    void invalidCasesProduceViolations() throws Exception {
        List<AuditEvent> violations = TestCheckSupportUtil.runTreeWalkerCheck(InlineRegexConstantCheck.class, "simplify/invalid/InlineRegexInvalid.java", Map.of());
        assertEquals(EXPECTED_VIOLATIONS, violations.size(), "Expected 5 inline regex violations, got: " + violations.size());
    }

    @Test
    void validCasesProduceNoViolations() throws Exception {
        assertTrue(TestCheckSupportUtil.runTreeWalkerCheck(InlineRegexConstantCheck.class, "simplify/valid/InlineRegexValid.java", Map.of()).isEmpty(), "Expected no violations");
    }
}
