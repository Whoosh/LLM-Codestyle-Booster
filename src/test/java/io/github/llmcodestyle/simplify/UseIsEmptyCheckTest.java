package io.github.llmcodestyle.simplify;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.github.llmcodestyle.utils.TestCheckSupportUtil.*;
import static org.junit.jupiter.api.Assertions.*;

class UseIsEmptyCheckTest {

    private static final int EXPECTED_VIOLATIONS = 14;
    private static final Map<String, String> NO_PROPS = Map.of();

    @Test
    void invalidCasesProduceViolations() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(UseIsEmptyCheck.class, "simplify/invalid/UseIsEmptyInvalid.java", NO_PROPS);
        assertEquals(EXPECTED_VIOLATIONS, violations.size(), "Expected 14 violations: " + format(violations));
    }

    @Test
    void validCasesProduceNoViolations() throws Exception {
        assertTrue(runTreeWalkerCheck(UseIsEmptyCheck.class, "simplify/valid/UseIsEmptyValid.java", NO_PROPS).isEmpty(), "Expected no violations");
    }

    @Test
    void violationMessageContainsMethodName() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(UseIsEmptyCheck.class, "simplify/invalid/UseIsEmptyInvalid.java", NO_PROPS);
        for (AuditEvent event : violations) {
            String msg = event.getMessage();
            assertTrue(msg.contains("length") || msg.contains("size"),
                "Message should mention length() or size(): " + msg);
        }
    }

    @Test
    void lengthViolationsDetected() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(UseIsEmptyCheck.class, "simplify/invalid/UseIsEmptyInvalid.java", NO_PROPS);
        long lengthCount = violations.stream().filter(v -> v.getMessage().contains("length")).count();
        assertTrue(lengthCount >= 5, "Expected at least 5 length() violations, got " + lengthCount);
    }

    @Test
    void sizeViolationsDetected() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(UseIsEmptyCheck.class, "simplify/invalid/UseIsEmptyInvalid.java", NO_PROPS);
        long sizeCount = violations.stream().filter(v -> v.getMessage().contains("size")).count();
        assertTrue(sizeCount >= 3, "Expected at least 3 size() violations, got " + sizeCount);
    }

    @Test
    void reversedOperandOrderDetected() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(UseIsEmptyCheck.class, "simplify/invalid/UseIsEmptyInvalid.java", NO_PROPS);
        // Lines 47-65 have reversed comparisons (0 < length, 0 == length, etc.)
        long reversedCount = violations.stream().filter(v -> v.getLine() >= 47 && v.getLine() <= 65).count();
        assertTrue(reversedCount >= 4, "Expected at least 4 reversed-operand violations, got " + reversedCount);
    }

    @Test
    void unwrapExprNodeAndMethodNameExtraction() throws Exception {
        // L106 SURVIVED: `node != null && node.getType() == EXPR` in unwrap
        // L86 NO_COVERAGE: `methodName` return "?" path
        // L107 NO_COVERAGE: `return node.getFirstChild()` in unwrap
        // Exercises size()/length() comparisons that involve EXPR-wrapped nodes.
        List<AuditEvent> violations = runTreeWalkerCheck(UseIsEmptyCheck.class,
            "simplify/invalid/UseIsEmptyUnwrap.java", NO_PROPS);
        assertEquals(2, violations.size(),
            "Both size() > 0 and 0 < size() should be flagged: " + format(violations));
        // Verify message contains "size" (not "?" which would indicate methodName failed)
        for (AuditEvent v : violations) {
            assertTrue(v.getMessage().contains("size"),
                "Message should contain 'size' method name: " + v.getMessage());
        }
    }
}
