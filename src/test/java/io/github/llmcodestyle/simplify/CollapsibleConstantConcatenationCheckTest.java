package io.github.llmcodestyle.simplify;

import io.github.llmcodestyle.TestCheckSupport;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CollapsibleConstantConcatenationCheckTest {

    private static final int EXPECTED_TOTAL_VIOLATIONS = 18;
    private static final int EXPECTED_ARRAY_VIOLATIONS = 4;
    private static final int EXPECTED_EDGE_CASE_VIOLATIONS = 5;
    private static final int EXPECTED_RUN_VIOLATIONS = 3;

    @Test
    void invalidCasesProduceViolations() throws Exception {
        List<AuditEvent> violations = TestCheckSupport.runTreeWalkerCheck(CollapsibleConstantConcatenationCheck.class, "invalid/CollapsibleConstantInvalid.java", Map.of());
        assertEquals(EXPECTED_TOTAL_VIOLATIONS, violations.size(), "Expected 18 violations, got: " + format(violations));
    }

    @Test
    void invalidMessagesContainFieldName() throws Exception {
        for (AuditEvent event : TestCheckSupport.runTreeWalkerCheck(CollapsibleConstantConcatenationCheck.class, "invalid/CollapsibleConstantInvalid.java", Map.of())) {
            assertTrue(
                event.getMessage().contains("collapsible") || event.getMessage().contains("single constant"),
                "Expected collapsible/constant mention in message, got: " + event.getMessage());
        }
    }

    @Test
    void tripleCountsThreeOperands() throws Exception {
        List<AuditEvent> violations = TestCheckSupport.runTreeWalkerCheck(CollapsibleConstantConcatenationCheck.class, "invalid/CollapsibleConstantInvalid.java", Map.of());
        AuditEvent tripleConcatEvent = violations.stream()
            .filter(e -> e.getMessage().contains("TRIPLE_CONCAT"))
            .findFirst()
            .orElse(null);
        assertTrue(tripleConcatEvent != null, "Expected violation for TRIPLE_CONCAT but not found in: " + format(violations));
        assertTrue(tripleConcatEvent.getMessage().contains("3"), "Expected operand count 3 in message: " + tripleConcatEvent.getMessage());
    }

    @Test
    void arrayElementConcatenationsFireSeparately() throws Exception {
        List<AuditEvent> violations = TestCheckSupport.runTreeWalkerCheck(CollapsibleConstantConcatenationCheck.class, "invalid/CollapsibleConstantInvalid.java", Map.of());
        long arrayViolations = violations.stream().filter(e -> e.getMessage().contains("Array element")).count();
        assertEquals(EXPECTED_ARRAY_VIOLATIONS, arrayViolations, "Expected 4 array element violations, got: " + arrayViolations + " — " + format(violations));
    }

    @Test
    void nestedClassAndEnumFireIndependently() throws Exception {
        List<AuditEvent> violations = TestCheckSupport.runTreeWalkerCheck(CollapsibleConstantConcatenationCheck.class, "invalid/CollapsibleConstantEdgeCases.java", Map.of());
        assertEquals(EXPECTED_EDGE_CASE_VIOLATIONS, violations.size(), "Expected 5 edge case violations, got: " + format(violations));
    }

    @Test
    void methodBodyRunsDetected() throws Exception {
        List<AuditEvent> violations = TestCheckSupport.runTreeWalkerCheck(CollapsibleConstantConcatenationCheck.class, "invalid/CollapsibleConstantInvalid.java", Map.of());
        long methodRunViolations = violations.stream().filter(e -> e.getMessage().contains("consecutive")).count();
        assertEquals(EXPECTED_RUN_VIOLATIONS, methodRunViolations, "Expected 3 method-body violations, got: " + methodRunViolations + " — " + format(violations));
    }

    @Test
    void recordWithMethodBodyRunDetected() throws Exception {
        List<AuditEvent> violations = TestCheckSupport.runTreeWalkerCheck(CollapsibleConstantConcatenationCheck.class, "invalid/CollapsibleRecordMethodRun.java", Map.of());
        long runViolations = violations.stream().filter(e -> e.getMessage().contains("consecutive")).count();
        assertTrue(runViolations >= 1, "Expected at least 1 method-body run violation in record, got: " + runViolations + " - all: " + format(violations));
    }

    @Test
    void validCasesProduceNoViolations() throws Exception {
        assertTrue(
            TestCheckSupport.runTreeWalkerCheck(
                CollapsibleConstantConcatenationCheck.class,
                "valid/CollapsibleConstantValid.java",
                Map.of()).isEmpty(),
            "Expected no violations");
    }

    private static String format(List<AuditEvent> violations) {
        return violations.stream()
            .map(e -> "Line " + e.getLine() + ": " + e.getMessage())
            .toList()
            .toString();
    }
}
