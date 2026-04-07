package io.github.llmcodestyle.simplify;

import io.github.llmcodestyle.TestCheckSupport;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class IndexOfToContainsCheckTest {

    private static final int EXPECTED_VIOLATIONS = 9;

    @Test
    void invalidCasesProduceViolations() throws Exception {
        List<AuditEvent> violations = TestCheckSupport.runTreeWalkerCheck(IndexOfToContainsCheck.class, "simplify/invalid/IndexOfToContainsInvalid.java", Map.of());
        assertEquals(EXPECTED_VIOLATIONS, violations.size(), "Expected 9 indexOf-vs-contains violations (5 normal + 4 reversed), got: " + violations.size());
    }

    @Test
    void validCasesProduceNoViolations() throws Exception {
        assertTrue(TestCheckSupport.runTreeWalkerCheck(IndexOfToContainsCheck.class, "simplify/valid/IndexOfToContainsValid.java", Map.of()).isEmpty(), "Expected no violations");
    }
}
