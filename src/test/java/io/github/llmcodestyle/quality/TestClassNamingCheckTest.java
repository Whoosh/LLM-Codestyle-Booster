package io.github.llmcodestyle.quality;

import io.github.llmcodestyle.TestCheckSupport;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestClassNamingCheckTest {

    @Test
    void invalidClassNameProducesViolation() throws Exception {
        List<AuditEvent> violations = TestCheckSupport.runTreeWalkerCheck(TestClassNamingCheck.class, "invalid/TestClassNamingInvalid.java", Map.of());
        assertEquals(1, violations.size(), "Expected 1 naming violation, got " + violations.size());
        assertTrue(violations.get(0).getMessage().contains("BadlyNamedHelper"), "Expected class name in message");
    }

    @Test
    void validClassNameProducesNoViolations() throws Exception {
        assertTrue(TestCheckSupport.runTreeWalkerCheck(TestClassNamingCheck.class, "valid/TestClassNamingValid.java", Map.of()).isEmpty(), "Expected no violations");
    }
}
