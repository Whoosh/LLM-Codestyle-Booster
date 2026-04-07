package io.github.llmcodestyle.simplify;

import io.github.llmcodestyle.TestCheckSupport;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UseIsEmptyCheckTest {

    private static final int EXPECTED_VIOLATIONS = 14;

    @Test
    void invalidCasesProduceViolations() throws Exception {
        List<AuditEvent> violations = TestCheckSupport.runTreeWalkerCheck(UseIsEmptyCheck.class, "simplify/invalid/UseIsEmptyInvalid.java", Map.of());
        assertEquals(EXPECTED_VIOLATIONS, violations.size(), "Expected 14 violations (5 length + 3 size + 5 reversed + 1 StringBuilder), got: " + violations.size());
    }

    @Test
    void validCasesProduceNoViolations() throws Exception {
        assertTrue(TestCheckSupport.runTreeWalkerCheck(UseIsEmptyCheck.class, "simplify/valid/UseIsEmptyValid.java", Map.of()).isEmpty(), "Expected no violations");
    }
}
