package io.github.llmcodestyle.simplify;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.github.llmcodestyle.utils.TestCheckSupportUtil.*;
import static org.junit.jupiter.api.Assertions.*;

class UseIsEmptyCheckTest {

    private static final int EXPECTED_VIOLATIONS = 14;
    private static final int MIN_LENGTH_VIOLATIONS = 5;
    private static final int MIN_SIZE_VIOLATIONS = 3;
    private static final int REVERSED_RANGE_START = 47;
    private static final int REVERSED_RANGE_END = 65;
    private static final int MIN_REVERSED_VIOLATIONS = 4;
    private static final Map<String, String> NO_PROPS = Map.of();

    @Test
    void invalidCasesProduceViolations() throws Exception {
        assertEquals(EXPECTED_VIOLATIONS, runInvalid().size());
    }

    @Test
    void validCasesProduceNoViolations() throws Exception {
        assertTrue(run("simplify/valid/UseIsEmptyValid.java").isEmpty(), "Expected no violations");
    }

    @Test
    void violationMessageContainsMethodName() throws Exception {
        for (AuditEvent event : runInvalid()) {
            String msg = event.getMessage();
            assertTrue(msg.contains("length") || msg.contains("size"), "Message should mention length() or size(): " + msg);
        }
    }

    @Test
    void lengthViolationsDetected() throws Exception {
        long lengthCount = runInvalid().stream()
            .filter(v -> v.getMessage().contains("length"))
            .count();
        assertTrue(lengthCount >= MIN_LENGTH_VIOLATIONS, "Expected at least 5 length() violations, got " + lengthCount);
    }

    @Test
    void sizeViolationsDetected() throws Exception {
        long sizeCount = runInvalid().stream()
            .filter(v -> v.getMessage().contains("size"))
            .count();
        assertTrue(sizeCount >= MIN_SIZE_VIOLATIONS, "Expected at least 3 size() violations, got " + sizeCount);
    }

    @Test
    void reversedOperandOrderDetected() throws Exception {
        // Lines 47-65 have reversed comparisons (0 < length, 0 == length, etc.)
        long reversedCount = runInvalid().stream()
            .filter(v -> v.getLine() >= REVERSED_RANGE_START && v.getLine() <= REVERSED_RANGE_END)
            .count();
        assertTrue(reversedCount >= MIN_REVERSED_VIOLATIONS, "Expected at least 4 reversed-operand violations, got " + reversedCount);
    }

    @Test
    void unwrapExprNodeAndMethodNameExtraction() throws Exception {
        // L106 SURVIVED: `node != null && node.getType() == EXPR` in unwrap
        // L86 NO_COVERAGE: `methodName` return "?" path
        // L107 NO_COVERAGE: `return node.getFirstChild()` in unwrap
        // Exercises size()/length() comparisons that involve EXPR-wrapped nodes.
        List<AuditEvent> violations = run("simplify/invalid/UseIsEmptyUnwrap.java");
        assertEquals(2, violations.size(), "Both size() > 0 and 0 < size() should be flagged: " + format(violations));
        // Verify message contains "size" (not "?" which would indicate methodName failed)
        for (AuditEvent v : violations) {
            assertTrue(v.getMessage().contains("size"), "Message should contain 'size' method name: " + v.getMessage());
        }
    }

    private static List<AuditEvent> run(String resource) throws Exception {
        return runTreeWalkerCheck(UseIsEmptyCheck.class, resource, NO_PROPS);
    }

    private static List<AuditEvent> runInvalid() throws Exception {
        return run("simplify/invalid/UseIsEmptyInvalid.java");
    }
}
