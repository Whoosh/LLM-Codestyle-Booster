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
    }

    @Test
    void tunedThresholdsRespected() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(DuplicateMethodBodyCheck.class, INVALID_A, LOOSER_PROPS);
        assertTrue(violations.stream().noneMatch(v -> v.getMessage().contains("walkBorNode")), "2-statement methods skipped: " + formatWithFile(violations));
    }

    private static List<AuditEvent> runSingle(String resource) throws Exception {
        return runTreeWalkerCheck(DuplicateMethodBodyCheck.class, resource, NO_PROPS);
    }

    private static List<AuditEvent> runMulti(String... resources) throws Exception {
        return runTreeWalkerCheckMultiFile(DuplicateMethodBodyCheck.class, List.of(resources), NO_PROPS);
    }

}
