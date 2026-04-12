package io.github.llmcodestyle.simplify;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.github.llmcodestyle.utils.TestCheckSupportUtil.*;
import static org.junit.jupiter.api.Assertions.*;

class CollapsibleConstantConcatenationCheckTest {

    private static final String INVALID_RESOURCE = "simplify/invalid/CollapsibleConstantInvalid.java";
    private static final String LINE_LENGTH_EDGE_RESOURCE = "simplify/invalid/CollapsibleConstantLineLengthEdge.java";
    private static final String ARRAY_INIT_RESOURCE = "simplify/invalid/CollapsibleConstantArrayInit.java";
    private static final int EXPECTED_TOTAL_VIOLATIONS = 18;
    private static final int EXPECTED_ARRAY_VIOLATIONS = 4;
    private static final int EXPECTED_EDGE_CASE_VIOLATIONS = 5;
    private static final int EXPECTED_RUN_VIOLATIONS = 3;
    private static final int MIN_LINE_LENGTH_EDGE_VIOLATIONS = 7;
    private static final int MIN_ARRAY_VIOLATIONS_LITERAL_NEW = 2;
    private static final int RUN_OF_5_LINE = 71;
    private static final int RUN_START_MAX_COLUMN = 30;
    private static final int DIRECT_CONCAT_OPERAND_COUNT = 2;

    @Test
    void invalidCasesProduceViolations() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(CollapsibleConstantConcatenationCheck.class, INVALID_RESOURCE, Map.of());
        assertEquals(EXPECTED_TOTAL_VIOLATIONS, violations.size(), "Expected 18 violations, got: " + format(violations));
    }

    @Test
    void invalidMessagesContainFieldName() throws Exception {
        for (AuditEvent event : runTreeWalkerCheck(CollapsibleConstantConcatenationCheck.class, INVALID_RESOURCE, Map.of())) {
            String msg = event.getMessage();
            assertTrue(msg.contains("collapsible") || msg.contains("single constant"), "Expected collapsible/constant mention in message, got: " + msg);
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
        List<AuditEvent> violations = runTreeWalkerCheck(CollapsibleConstantConcatenationCheck.class, LINE_LENGTH_EDGE_RESOURCE, Map.of());
        // SUM_OF_NUMS (int + int), SHORT (str + str), EXPLICIT_NEW_ARRAY (2 elements),
        // FIT_EASY (str + str), COMBO (interface), EXPR_WRAP (parens), MIXED (str + int)
        assertTrue(violations.size() >= MIN_LINE_LENGTH_EDGE_VIOLATIONS, "Expected at least 7 violations for line-length edge cases, got: " + format(violations));
    }

    @Test
    void explicitNewArrayInitFiresViolations() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(CollapsibleConstantConcatenationCheck.class, LINE_LENGTH_EDGE_RESOURCE, Map.of());
        // The 'new String[] { "alpha" + "_bravo", "charlie" + "_delta" }' should fire 2 array violations
        long arrayViolations = violations.stream().filter(e -> e.getMessage().contains("Array element")).count();
        assertTrue(arrayViolations >= MIN_ARRAY_VIOLATIONS_LITERAL_NEW,
            "Expected at least 2 array element violations via LITERAL_NEW, got: " + arrayViolations + " — " + format(violations));
    }

    @Test
    void longConcatenationExceedingLineLengthIsNotFlagged() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(CollapsibleConstantConcatenationCheck.class, "simplify/valid/CollapsibleConstantLineLengthValid.java", Map.of());
        assertTrue(violations.isEmpty(), "Concatenation exceeding 180-char line length should not be flagged: " + format(violations));
    }

    @Test
    void methodBodyRunViolationsReportCorrectRunLength() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(CollapsibleConstantConcatenationCheck.class, INVALID_RESOURCE, Map.of());
        // Case 15: run of 5, Case 16: run of 3, Case 17: run of 2
        List<AuditEvent> runViolations = violations.stream().filter(e -> e.getMessage().contains("consecutive")).toList();
        assertEquals(EXPECTED_RUN_VIOLATIONS, runViolations.size(), "Expected 3 run violations: " + format(violations));
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
        // Array violations should mention the array field name (kills checkArrayInit ident mutation)
        List<AuditEvent> arrayViolations = runTreeWalkerCheck(CollapsibleConstantConcatenationCheck.class, INVALID_RESOURCE, Map.of())
            .stream()
            .filter(e -> e.getMessage().contains("Array element"))
            .toList();
        assertTrue(arrayViolations.stream().anyMatch(v -> v.getMessage().contains("BANNED")), "Array violation should mention BANNED: " + format(arrayViolations));
        assertTrue(arrayViolations.stream().anyMatch(v -> v.getMessage().contains("PATHS")), "Array violation should mention PATHS: " + format(arrayViolations));
    }

    @Test
    void scalarFieldViolationCountsOperands() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(CollapsibleConstantConcatenationCheck.class, INVALID_RESOURCE, Map.of());
        // DIRECT_CONCAT has 2 operands, TRIPLE_CONCAT has 3
        AuditEvent direct = violations.stream()
            .filter(v -> v.getMessage().contains("DIRECT_CONCAT"))
            .findFirst()
            .orElse(null);
        assertNotNull(direct, "DIRECT_CONCAT violation expected: " + format(violations));
        assertTrue(direct.getMessage().contains(String.valueOf(DIRECT_CONCAT_OPERAND_COUNT)), "DIRECT_CONCAT should report 2 operands: " + direct.getMessage());
    }

    @Test
    void mixedStringAndNumericNotAllString() throws Exception {
        // MIXED = "prefix" + 42 should fire (int is a single literal, both are collapsible)
        // but mergedLiteralWouldFitOnLine handles non-string by returning contentLength < 0
        List<AuditEvent> violations = runTreeWalkerCheck(CollapsibleConstantConcatenationCheck.class, LINE_LENGTH_EDGE_RESOURCE, Map.of());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("MIXED")), "Mixed string+int concat should be flagged: " + format(violations));
    }

    @Test
    void interfaceFieldsConcatenationsDetected() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(CollapsibleConstantConcatenationCheck.class, LINE_LENGTH_EDGE_RESOURCE, Map.of());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("COMBO")), "Interface field concat should be flagged: " + format(violations));
    }

    @Test
    void violationLinesArePositive() throws Exception {
        for (AuditEvent v : runTreeWalkerCheck(CollapsibleConstantConcatenationCheck.class, INVALID_RESOURCE, Map.of())) {
            assertTrue(v.getLine() > 0, "Line should be positive: " + v);
        }
    }

    @Test
    void methodBodyRunReportsFirstLeafPosition() throws Exception {
        List<AuditEvent> runViolations = runTreeWalkerCheck(CollapsibleConstantConcatenationCheck.class, INVALID_RESOURCE, Map.of())
            .stream()
            .filter(e -> e.getMessage().contains("consecutive"))
            .toList();
        // checkForCollapsibleRun line 257: if runLength == 0 is negated, runStart is wrong
        // The run-of-5 violation (line 71) should point to INSERT_PREFIX, not DEFAULT_TABLE
        AuditEvent runOf5 = runViolations.stream()
            .filter(v -> v.getMessage().contains("5"))
            .findFirst()
            .orElse(null);
        assertNotNull(runOf5, "Should have run of 5: " + format(runViolations));
        assertEquals(RUN_OF_5_LINE, runOf5.getLine(), "Run of 5 should be on line 71: " + runOf5);
        // INSERT_PREFIX is at column 15 (after "return "), not at DEFAULT_TABLE position
        assertTrue(runOf5.getColumn() < RUN_START_MAX_COLUMN, "Run should start at INSERT_PREFIX position (early column): " + runOf5.getColumn());
    }

    @Test
    void declarationPrefixLengthAffectsLineCheck() throws Exception {
        // declarationPrefixLength line 49: eqIdx < 0. If the field has '=' sign (eqIdx >= 0),
        // prefix = eqIdx + 2. If negated, prefix would be wrong for fields with '='.
        List<AuditEvent> violations = runTreeWalkerCheck(CollapsibleConstantConcatenationCheck.class, LINE_LENGTH_EDGE_RESOURCE, Map.of());
        // The SHORT field ("a" + "b") should be flagged because the merged literal fits on one line
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("SHORT")), "Short concat should be flagged: " + format(violations));
    }

    @Test
    void findArrayInitWithLiteralNew() throws Exception {
        // L197 NO_COVERAGE: `return node` (ARRAY_INIT path) and LITERAL_NEW -> findFirstToken(ARRAY_INIT) path
        List<AuditEvent> violations = runTreeWalkerCheck(CollapsibleConstantConcatenationCheck.class, ARRAY_INIT_RESOURCE, Map.of());
        // ITEMS = new String[] {"a" + "b", "c"} — should detect the array element concatenation
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Array element")), "Array init via LITERAL_NEW should detect concatenation: " + format(violations));
    }

    @Test
    void scanForCollapsibleRunsTopLevelPlus() throws Exception {
        // L250 SURVIVED: `node.getType() == PLUS && !(node.getParent() != null && node.getParent().getType() == PLUS)`
        // Exercises the top-level PLUS detection in method bodies
        List<AuditEvent> violations = runTreeWalkerCheck(CollapsibleConstantConcatenationCheck.class, ARRAY_INIT_RESOURCE, Map.of());
        // methodWithConcats: "hello" + " " + "world" + compute() — run of 3 literals before compute()
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("consecutive")), "Method body with collapsible literal run should be flagged: " + format(violations));
    }

}
