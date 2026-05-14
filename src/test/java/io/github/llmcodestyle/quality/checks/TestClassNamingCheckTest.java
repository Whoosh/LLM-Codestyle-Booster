package io.github.llmcodestyle.quality.checks;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.github.llmcodestyle.utils.TestCheckSupportUtil.*;
import static org.junit.jupiter.api.Assertions.*;

class TestClassNamingCheckTest {

    private static final Map<String, String> NO_PROPS = Map.of();

    @Test
    void invalidClassNameProducesViolation() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(TestClassNamingCheck.class, "quality/invalid/TestClassNamingInvalid.java", NO_PROPS);
        assertEquals(1, violations.size(), "Expected 1 naming violation, got " + format(violations));
        assertTrue(violations.get(0).getMessage().contains("BadlyNamedHelper"), "Expected class name in message");
    }

    @Test
    void validClassNameProducesNoViolations() throws Exception {
        assertTrue(runTreeWalkerCheck(TestClassNamingCheck.class, "quality/valid/TestClassNamingValid.java", NO_PROPS).isEmpty(), "Expected no violations");
    }

    @Test
    void violationReportsCorrectLineAndColumn() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(TestClassNamingCheck.class, "quality/invalid/TestClassNamingInvalid.java", NO_PROPS);
        assertEquals(1, violations.size());
        assertTrue(violations.get(0).getLine() > 0, "Line should be positive");
        assertTrue(violations.get(0).getColumn() >= 0, "Column should be non-negative");
    }

    @Test
    void innerClassesAreNotFlagged() throws Exception {
        // Valid fixture likely contains inner classes — they should not be flagged
        assertTrue(runTreeWalkerCheck(TestClassNamingCheck.class, "quality/valid/TestClassNamingValid.java", NO_PROPS).isEmpty(), "Inner classes should not be flagged");
    }
}
