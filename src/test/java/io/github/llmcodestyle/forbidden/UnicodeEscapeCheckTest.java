package io.github.llmcodestyle.forbidden;

import io.github.llmcodestyle.TestCheckSupport;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UnicodeEscapeCheckTest {

    private static final int EXPECTED_VIOLATIONS = 3;

    @Test
    void invalidCasesProduceViolations() throws Exception {
        List<AuditEvent> violations = TestCheckSupport.runFileSetCheck(UnicodeEscapeCheck.class, "forbidden/invalid/UnicodeEscapeInvalid.java", Map.of());
        assertEquals(EXPECTED_VIOLATIONS, violations.size(), "Expected 3 unicode escape violations, got: " + violations.size());
    }

    @Test
    void validCasesProduceNoViolations() throws Exception {
        assertTrue(TestCheckSupport.runFileSetCheck(UnicodeEscapeCheck.class, "forbidden/valid/UnicodeEscapeValid.java", Map.of()).isEmpty(), "Expected no violations");
    }
}
