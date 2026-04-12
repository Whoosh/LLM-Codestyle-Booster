package io.github.llmcodestyle.simplify;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.github.llmcodestyle.utils.TestCheckSupportUtil.*;
import static org.junit.jupiter.api.Assertions.*;

class CollapsibleConstantConcatenationCheckTest {

    private static final String INVALID_RESOURCE = "simplify/invalid/CollapsibleConstantInvalid.java";
    private static final int EXPECTED_TOTAL_VIOLATIONS = 18;
    private static final int EXPECTED_ARRAY_VIOLATIONS = 4;
    private static final int EXPECTED_EDGE_CASE_VIOLATIONS = 5;
    private static final int EXPECTED_RUN_VIOLATIONS = 3;

    @Test
    void invalidCasesProduceViolations() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(CollapsibleConstantConcatenationCheck.class, INVALID_RESOURCE, Map.of());
        assertEquals(EXPECTED_TOTAL_VIOLATIONS, violations.size(), "Expected 18 violations, got: " + format(violations));
    }

    @Test
    void invalidMessagesContainFieldName() throws Exception {
        for (AuditEvent event : runTreeWalkerCheck(CollapsibleConstantConcatenationCheck.class, INVALID_RESOURCE, Map.of())) {
            assertTrue(
                event.getMessage().contains("collapsible") || event.getMessage().contains("single constant"),
                "Expected collapsible/constant mention in message, got: " + event.getMessage());
        }
    }

    @Test
    void tripleCountsThreeOperands() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(CollapsibleConstantConcatenationCheck.class, INVALID_RESOURCE, Map.of());
        AuditEvent tripleConcatEvent = violations.stream()
            .filter(e -> e.getMessage().contains("TRIPLE_CONCAT"))
            .findFirst()
            .orElse(null);
        assertTrue(tripleConcatEvent != null, "Expected violation for TRIPLE_CONCAT but not found in: " + format(violations));
        assertTrue(tripleConcatEvent.getMessage().contains("3"), "Expected operand count 3 in message: " + tripleConcatEvent.getMessage());
    }

    @Test
    void arrayElementConcatenationsFireSeparately() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(CollapsibleConstantConcatenationCheck.class, INVALID_RESOURCE, Map.of());
        long arrayViolations = violations.stream().filter(e -> e.getMessage().contains("Array element")).count();
        assertEquals(EXPECTED_ARRAY_VIOLATIONS, arrayViolations, "Expected 4 array element violations, got: " + arrayViolations + " — " + format(violations));
    }

    @Test
    void nestedClassAndEnumFireIndependently() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(CollapsibleConstantConcatenationCheck.class, "simplify/invalid/CollapsibleConstantEdgeCases.java", Map.of());
        assertEquals(EXPECTED_EDGE_CASE_VIOLATIONS, violations.size(), "Expected 5 edge case violations, got: " + format(violations));
    }

    @Test
    void methodBodyRunsDetected() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(CollapsibleConstantConcatenationCheck.class, INVALID_RESOURCE, Map.of());
        long methodRunViolations = violations.stream().filter(e -> e.getMessage().contains("consecutive")).count();
        assertEquals(EXPECTED_RUN_VIOLATIONS, methodRunViolations, "Expected 3 method-body violations, got: " + methodRunViolations + " — " + format(violations));
    }

    @Test
    void recordWithMethodBodyRunDetected() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(CollapsibleConstantConcatenationCheck.class, "simplify/invalid/CollapsibleRecordMethodRun.java", Map.of());
        long runViolations = violations.stream().filter(e -> e.getMessage().contains("consecutive")).count();
        assertTrue(runViolations >= 1, "Expected at least 1 method-body run violation in record, got: " + runViolations + " - all: " + format(violations));
    }

    @Test
    void validCasesProduceNoViolations() throws Exception {
        assertTrue(runTreeWalkerCheck(CollapsibleConstantConcatenationCheck.class, "simplify/valid/CollapsibleConstantValid.java", Map.of()).isEmpty(), "Expected no violations");
    }

    @Test
    void lineLengthEdgeCasesProduceViolations() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(CollapsibleConstantConcatenationCheck.class,
            "simplify/invalid/CollapsibleConstantLineLengthEdge.java", Map.of());
        // SUM_OF_NUMS (int + int), SHORT (str + str), EXPLICIT_NEW_ARRAY (2 elements),
        // FIT_EASY (str + str), COMBO (interface), EXPR_WRAP (parens), MIXED (str + int)
        assertTrue(violations.size() >= 7,
            "Expected at least 7 violations for line-length edge cases, got: " + format(violations));
    }

    @Test
    void explicitNewArrayInitFiresViolations() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(CollapsibleConstantConcatenationCheck.class,
            "simplify/invalid/CollapsibleConstantLineLengthEdge.java", Map.of());
        // The 'new String[] { "alpha" + "_bravo", "charlie" + "_delta" }' should fire 2 array violations
        long arrayViolations = violations.stream().filter(e -> e.getMessage().contains("Array element")).count();
        assertTrue(arrayViolations >= 2, "Expected at least 2 array element violations via LITERAL_NEW, got: " + arrayViolations + " — " + format(violations));
    }

    @Test
    void longConcatenationExceedingLineLengthIsNotFlagged() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(CollapsibleConstantConcatenationCheck.class,
            "simplify/valid/CollapsibleConstantLineLengthValid.java", Map.of());
        assertTrue(violations.isEmpty(),
            "Concatenation exceeding 180-char line length should not be flagged: " + format(violations));
    }

    @Test
    void methodBodyRunViolationsReportCorrectRunLength() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(CollapsibleConstantConcatenationCheck.class, INVALID_RESOURCE, Map.of());
        // Case 15: run of 5, Case 16: run of 3, Case 17: run of 2
        List<AuditEvent> runViolations = violations.stream().filter(e -> e.getMessage().contains("consecutive")).toList();
        assertEquals(3, runViolations.size(), "Expected 3 run violations: " + format(violations));
        assertTrue(runViolations.stream().anyMatch(v -> v.getMessage().contains("5")), "Should have run of 5: " + format(runViolations));
        assertTrue(runViolations.stream().anyMatch(v -> v.getMessage().contains("3")), "Should have run of 3: " + format(runViolations));
        assertTrue(runViolations.stream().anyMatch(v -> v.getMessage().contains("2")), "Should have run of 2: " + format(runViolations));
    }

    @Test
    void eachFieldViolationReportsFieldName() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(CollapsibleConstantConcatenationCheck.class, INVALID_RESOURCE, Map.of());
        // Check that specific field names appear in violations (kills SURVIVED mutations on field name extraction)
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("DIRECT_CONCAT")), format(violations));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("PATH")), format(violations));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("URL")), format(violations));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("TOTAL")), format(violations));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("AREA")), format(violations));
    }

    @Test
    void arrayViolationsReportFieldName() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(CollapsibleConstantConcatenationCheck.class, INVALID_RESOURCE, Map.of());
        // Array violations should mention the array field name (kills checkArrayInit ident mutation)
        List<AuditEvent> arrayViolations = violations.stream().filter(e -> e.getMessage().contains("Array element")).toList();
        assertTrue(arrayViolations.stream().anyMatch(v -> v.getMessage().contains("BANNED")),
            "Array violation should mention BANNED: " + format(arrayViolations));
        assertTrue(arrayViolations.stream().anyMatch(v -> v.getMessage().contains("PATHS")),
            "Array violation should mention PATHS: " + format(arrayViolations));
    }

    @Test
    void scalarFieldViolationCountsOperands() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(CollapsibleConstantConcatenationCheck.class, INVALID_RESOURCE, Map.of());
        // DIRECT_CONCAT has 2 operands, TRIPLE_CONCAT has 3
        AuditEvent direct = violations.stream().filter(v -> v.getMessage().contains("DIRECT_CONCAT")).findFirst().orElse(null);
        assertNotNull(direct, "DIRECT_CONCAT violation expected: " + format(violations));
        assertTrue(direct.getMessage().contains("2"), "DIRECT_CONCAT should report 2 operands: " + direct.getMessage());
    }

    @Test
    void mixedStringAndNumericNotAllString() throws Exception {
        // MIXED = "prefix" + 42 should fire (int is a single literal, both are collapsible)
        // but mergedLiteralWouldFitOnLine handles non-string by returning contentLength < 0
        List<AuditEvent> violations = runTreeWalkerCheck(CollapsibleConstantConcatenationCheck.class,
            "simplify/invalid/CollapsibleConstantLineLengthEdge.java", Map.of());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("MIXED")),
            "Mixed string+int concat should be flagged: " + format(violations));
    }

    @Test
    void interfaceFieldsConcatenationsDetected() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(CollapsibleConstantConcatenationCheck.class,
            "simplify/invalid/CollapsibleConstantLineLengthEdge.java", Map.of());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("COMBO")),
            "Interface field concat should be flagged: " + format(violations));
    }

    @Test
    void violationLinesArePositive() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(CollapsibleConstantConcatenationCheck.class, INVALID_RESOURCE, Map.of());
        for (AuditEvent v : violations) {
            assertTrue(v.getLine() > 0, "Line should be positive: " + v);
        }
    }

}
