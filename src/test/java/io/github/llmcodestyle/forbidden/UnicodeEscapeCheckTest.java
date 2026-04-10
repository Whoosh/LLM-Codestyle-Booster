package io.github.llmcodestyle.forbidden;

import io.github.llmcodestyle.utils.TestCheckSupportUtil;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class UnicodeEscapeCheckTest {

    private static final int EXPECTED_VIOLATIONS = 3;

    @Test
    void invalidCasesProduceViolations() throws Exception {
        List<AuditEvent> violations = TestCheckSupportUtil.runFileSetCheck(UnicodeEscapeCheck.class, "forbidden/invalid/UnicodeEscapeInvalid.java", Map.of());
        assertEquals(EXPECTED_VIOLATIONS, violations.size(), "Expected 3 unicode escape violations, got: " + violations.size());
    }

    @Test
    void validCasesProduceNoViolations() throws Exception {
        assertTrue(TestCheckSupportUtil.runFileSetCheck(UnicodeEscapeCheck.class, "forbidden/valid/UnicodeEscapeValid.java", Map.of()).isEmpty(), "Expected no violations");
    }
}
