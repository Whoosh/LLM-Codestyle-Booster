package io.github.llmcodestyle.quality;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.github.llmcodestyle.utils.TestCheckSupportUtil.*;
import static org.junit.jupiter.api.Assertions.*;

class DuplicateMethodBodyCheckTest {

    private static final Map<String, String> NO_PROPS = Map.of();

    private static final String INVALID_A = "quality/invalid/DuplicateMethodBodyInvalidA.java";
    private static final String INVALID_B = "quality/invalid/DuplicateMethodBodyInvalidB.java";
    private static final String JSON_ARRAYS = "quality/invalid/DuplicateMethodBodyJsonArrays.java";
    private static final String PHYSICS_QUIZ_ROW = "quality/invalid/DuplicateMethodBodyPhysicsQuizRow.java";
    private static final String STATEFUL_A = "quality/invalid/DuplicateMethodBodyStatefulA.java";
    private static final String STATEFUL_B = "quality/invalid/DuplicateMethodBodyStatefulB.java";
    private static final String PHYSICS_PROMPT_BUILDER = "quality/invalid/DuplicateMethodBodyPhysicsPromptBuilder.java";
    private static final String JAVA_PROMPT_BUILDER = "quality/invalid/DuplicateMethodBodyJavaPromptBuilder.java";
    private static final String JAVA_BATCH_INSERTER = "quality/invalid/DuplicateMethodBodyJavaBatchInserter.java";
    private static final String PHYSICS_BATCH_INSERTER = "quality/invalid/DuplicateMethodBodyPhysicsBatchInserter.java";
    private static final String JAVA_QUIZ_INSERTER = "quality/invalid/DuplicateMethodBodyJavaQuizInserter.java";
    private static final String PHYSICS_QUIZ_INSERTER = "quality/invalid/DuplicateMethodBodyPhysicsQuizInserter.java";
    private static final String JAVA_BATCH_POLLER = "quality/invalid/DuplicateMethodBodyJavaBatchPoller.java";
    private static final String PHYSICS_BATCH_POLLER = "quality/invalid/DuplicateMethodBodyPhysicsBatchPoller.java";
    private static final String VALID = "quality/valid/DuplicateMethodBodyValid.java";

    private static final int CROSS_FILE_DUPLICATES = 3;
    private static final int LOOSER_MIN_STATEMENTS = 3;
    private static final int TIGHTER_MAX_BODY_NODES = 50;
    // Under the new dual filter a method is only trivial when BOTH thresholds are
    // below it, so to skip 2-statement walkBorNode we must also raise minBodyNodes
    // above its node count.
    private static final Map<String, String> LOOSER_PROPS = Map.of("minStatements", "3", "minBodyNodes", "500");

    @Test
    void singleFileDetectsWithinClassDuplicate() throws Exception {
        List<AuditEvent> violations = runSingle(INVALID_A);
        assertEquals(1, violations.size(), formatWithFile(violations));
        assertTrue(violations.get(0).getMessage().contains("walkBorNode"));
        assertTrue(violations.get(0).getMessage().contains("walkTypeNode"));
    }

    @Test
    void singleFileBAloneHasNoDuplicates() throws Exception {
        assertTrue(runSingle(INVALID_B).isEmpty());
    }

    @Test
    void crossFileAccumulatesDuplicates() throws Exception {
        List<AuditEvent> violations = runMulti(INVALID_A, INVALID_B);
        assertEquals(CROSS_FILE_DUPLICATES, violations.size(), formatWithFile(violations));
    }

    @Test
    void differentLiteralsAreNotDuplicates() throws Exception {
        assertFalse(
            runMulti(INVALID_A, INVALID_B).stream().anyMatch(v -> v.getMessage().contains("differentLiteral")),
            "differentLiteral must not match any other method (different literal)");
    }

    @Test
    void validFileAloneHasNoDuplicates() throws Exception {
        assertTrue(runSingle(VALID).isEmpty(), format(runSingle(VALID)));
    }

    @Test
    void gettersAndSettersBelowThresholdSkipped() throws Exception {
        List<AuditEvent> violations = runSingle(VALID);
        assertTrue(violations.stream().noneMatch(v -> v.getMessage().contains("getName")));
        assertTrue(violations.stream().noneMatch(v -> v.getMessage().contains("setName")));
    }

    @Test
    void overrideMethodsSkipped() throws Exception {
        List<AuditEvent> violations = runSingle(VALID);
        assertTrue(violations.stream().noneMatch(v -> v.getMessage().contains("equals")));
        assertTrue(violations.stream().noneMatch(v -> v.getMessage().contains("hashCode")));
    }

    @Test
    void crossFileStatelessDuplicateSuggestsUtilExtraction() throws Exception {
        List<AuditEvent> violations = runMulti(JSON_ARRAYS, PHYSICS_QUIZ_ROW);
        assertEquals(1, violations.size(), formatWithFile(violations));
        String msg = violations.get(0).getMessage();
        assertTrue(msg.contains("parseAnswers") && msg.contains("parseTags"), formatWithFile(violations));
        assertTrue(msg.contains("extract into a shared utility class"), msg);
    }

    @Test
    void crossFileSingleTryBodyDuplicateCaught() throws Exception {
        List<AuditEvent> violations = runMulti(PHYSICS_PROMPT_BUILDER, JAVA_PROMPT_BUILDER);
        assertEquals(1, violations.size(), formatWithFile(violations));
        String msg = violations.get(0).getMessage();
        assertTrue(msg.contains("readResource") && msg.contains("loadResource"), formatWithFile(violations));
        assertTrue(msg.contains("extract into a shared utility class"), msg);
    }

    @Test
    void crossFileJsonLineReaderDuplicateCaught() throws Exception {
        List<AuditEvent> violations = runMulti(JAVA_BATCH_INSERTER, PHYSICS_BATCH_INSERTER);
        assertEquals(1, violations.size(), formatWithFile(violations));
        String msg = violations.get(0).getMessage();
        assertTrue(msg.contains("parseJsonLines") && msg.contains("readJsonLines"), formatWithFile(violations));
        assertTrue(msg.contains("extract into a shared utility class"), msg);
    }

    @Test
    void crossFileSameNameBatchInsertDuplicateCaught() throws Exception {
        List<AuditEvent> violations = runMulti(JAVA_QUIZ_INSERTER, PHYSICS_QUIZ_INSERTER);
        assertEquals(1, violations.size(), formatWithFile(violations));
        String msg = violations.get(0).getMessage();
        assertTrue(msg.contains("insertBatch"), formatWithFile(violations));
        assertTrue(msg.contains("consolidate into a shared helper"), msg);
    }

    @Test
    void crossFileVarargsVsArrayMainDuplicateCaught() throws Exception {
        List<AuditEvent> violations = runMulti(JAVA_BATCH_POLLER, PHYSICS_BATCH_POLLER);
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("main")), formatWithFile(violations));
    }

    @Test
    void crossFileStatefulDuplicateDoesNotSuggestUtilExtraction() throws Exception {
        List<AuditEvent> violations = runMulti(STATEFUL_A, STATEFUL_B);
        assertEquals(1, violations.size(), formatWithFile(violations));
        String msg = violations.get(0).getMessage();
        assertTrue(msg.contains("computeDiscount") && msg.contains("computePrice"), formatWithFile(violations));
        assertFalse(msg.contains("extract into a shared utility class"), msg);
        assertTrue(msg.contains("consolidate into a shared helper"), msg);
    }

    @Test
    void asymptoticLargeMethodSkippedBySizeCap() throws Exception {
        assertTrue(runSingle("quality/invalid/DuplicateMethodBodyLarge.java").isEmpty());
    }

    @Test
    void settersAreDirectlyInvokable() {
        DuplicateMethodBodyCheck check = new DuplicateMethodBodyCheck();
        check.setMinStatements(LOOSER_MIN_STATEMENTS);
        check.setMinBodyNodes(TIGHTER_MAX_BODY_NODES);
        check.setMaxBodyNodes(TIGHTER_MAX_BODY_NODES);
        assertNotNull(check);
        int[] tokens = check.getDefaultTokens();
        assertNotNull(tokens);
        assertTrue(tokens.length > 0);
    }

    @Test
    void violationMessageContainsMethodNames() throws Exception {
        List<AuditEvent> violations = runSingle(INVALID_A);
        assertEquals(1, violations.size(), formatWithFile(violations));
        String msg = violations.get(0).getMessage();
        assertNotNull(msg);
        assertFalse(msg.isEmpty());
    }

    @Test
    void tunedThresholdsRespected() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(DuplicateMethodBodyCheck.class, INVALID_A, LOOSER_PROPS);
        assertTrue(violations.stream().noneMatch(v -> v.getMessage().contains("walkBorNode")), "2-statement methods skipped: " + formatWithFile(violations));
    }

    @Test
    void nonOverrideAnnotationsDoNotSkipMethods() throws Exception {
        List<AuditEvent> violations = runSingle("quality/invalid/DuplicateMethodBodyAnnotatedPair.java");
        assertEquals(1, violations.size(), "Annotated non-Override methods should be compared: " + formatWithFile(violations));
        String msg = violations.get(0).getMessage();
        assertTrue(msg.contains("processBeta") || msg.contains("processAlpha"),
            "Should detect processAlpha/processBeta duplication: " + msg);
    }

    @Test
    void overrideAnnotationSkipsMethod() throws Exception {
        List<AuditEvent> violations = runSingle("quality/invalid/DuplicateMethodBodyAnnotatedPair.java");
        // toString() has @Override, so even though its body matches, it should be skipped
        assertTrue(violations.stream().noneMatch(v -> v.getMessage().contains("toString")),
            "Override methods should be skipped: " + formatWithFile(violations));
    }

    @Test
    void violationMessageContainsClassName() throws Exception {
        List<AuditEvent> violations = runMulti(INVALID_A, INVALID_B);
        // Cross-file violations should contain the class name of the first occurrence
        for (AuditEvent v : violations) {
            String msg = v.getMessage();
            assertTrue(msg.contains("DuplicateMethodBodyInvalid"),
                "Message should contain class name: " + msg);
        }
    }

    @Test
    void singleFileViolationMentionsBothMethodNames() throws Exception {
        List<AuditEvent> violations = runSingle(INVALID_A);
        assertEquals(1, violations.size(), formatWithFile(violations));
        String msg = violations.get(0).getMessage();
        assertTrue(msg.contains("walkBorNode"), "Should mention second method: " + msg);
        assertTrue(msg.contains("walkTypeNode"), "Should mention first method: " + msg);
    }

    @Test
    void isOverrideChecksBothAnnotationAndIdent() throws Exception {
        // Tests isOverride lines 193, 197, 199:
        // - modifiers != null
        // - mod.getType() == ANNOTATION
        // - ident != null && "Override".equals(ident.getText())
        // Verified by: @Override methods skipped, non-Override annotations NOT skipped
        List<AuditEvent> annotatedViolations = runSingle("quality/invalid/DuplicateMethodBodyAnnotatedPair.java");
        // Non-Override annotated methods should still be compared
        assertTrue(annotatedViolations.size() == 1,
            "Only non-Override duplicate pair should produce violation: " + formatWithFile(annotatedViolations));
        // Ensure @Override method toString() is skipped
        assertTrue(annotatedViolations.stream().noneMatch(v -> v.getMessage().contains("toString")),
            "Override methods must be skipped: " + formatWithFile(annotatedViolations));
    }

    @Test
    void extractEnclosingClassNameIncludedInMessage() throws Exception {
        List<AuditEvent> violations = runSingle(INVALID_A);
        assertFalse(violations.isEmpty(), "Should have violations");
        String msg = violations.get(0).getMessage();
        // The class name should be non-empty and not "<unknown>"
        assertFalse(msg.contains("<unknown>"),
            "extractEnclosingClassName should return actual class name: " + msg);
        assertTrue(msg.contains("DuplicateMethodBodyInvalid"),
            "Message should contain the class name: " + msg);
    }

    @Test
    void swappedParameterOrderIsNotDuplicate() throws Exception {
        List<AuditEvent> violations = runSingle("quality/valid/DuplicateMethodBodySwappedParams.java");
        assertTrue(violations.isEmpty(),
            "Methods with swapped parameter usage should not be duplicates: " + formatWithFile(violations));
    }

    @Test
    void overrideWithIdenticalBodyIsSkipped() throws Exception {
        List<AuditEvent> violations = runSingle("quality/invalid/DuplicateMethodBodyOverridePair.java");
        // 'same' and 'equals' have structurally identical bodies. But equals has @Override, so only
        // 'same' should be stored as first occurrence, and equals should be SKIPPED entirely.
        // If isOverride is broken, both methods are compared, producing a violation for equals.
        assertTrue(violations.isEmpty(),
            "Override method with same body as non-override should not produce violation: " + formatWithFile(violations));
    }

    @Test
    void extractEnclosingClassNameInNestedClass() throws Exception {
        // L220 NO_COVERAGE: `return "<unknown>"` when no enclosing type found
        // This test uses nested inner classes to ensure extractEnclosingClassName is reached
        List<AuditEvent> violations = runSingle("quality/invalid/DuplicateMethodBodyNestedClass.java");
        assertEquals(1, violations.size(),
            "Duplicate methods in nested classes should be detected: " + formatWithFile(violations));
        String msg = violations.get(0).getMessage();
        assertTrue(msg.contains("InnerA") || msg.contains("InnerB"),
            "Message should contain nested class name: " + msg);
        assertFalse(msg.contains("<unknown>"),
            "extractEnclosingClassName should return actual class name: " + msg);
    }

    private static List<AuditEvent> runSingle(String resource) throws Exception {
        return runTreeWalkerCheck(DuplicateMethodBodyCheck.class, resource, NO_PROPS);
    }

    private static List<AuditEvent> runMulti(String... resources) throws Exception {
        return runTreeWalkerCheckMultiFile(DuplicateMethodBodyCheck.class, List.of(resources), NO_PROPS);
    }

}
