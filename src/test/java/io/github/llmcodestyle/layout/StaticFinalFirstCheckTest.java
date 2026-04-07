package io.github.llmcodestyle.layout;

import io.github.llmcodestyle.TestCheckSupport;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class StaticFinalFirstCheckTest {

    private static final int EXPECTED_VIOLATIONS = 2;

    @Test
    void invalidCasesProduceViolations() throws Exception {
        List<AuditEvent> violations = TestCheckSupport.runTreeWalkerCheck(StaticFinalFirstCheck.class, "layout/invalid/StaticFinalFirstInvalid.java", Map.of());
        assertEquals(EXPECTED_VIOLATIONS, violations.size(), "Expected 2 violations (2 static finals after instance field), got: " + violations.size());
    }

    @Test
    void validCasesProduceNoViolations() throws Exception {
        assertTrue(TestCheckSupport.runTreeWalkerCheck(StaticFinalFirstCheck.class, "layout/valid/StaticFinalFirstValid.java", Map.of()).isEmpty(), "Expected no violations");
    }
}
