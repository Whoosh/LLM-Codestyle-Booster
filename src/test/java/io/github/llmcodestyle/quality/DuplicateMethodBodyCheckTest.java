package io.github.llmcodestyle.quality;

import io.github.llmcodestyle.utils.TestCheckSupportUtil;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DuplicateMethodBodyCheckTest {

    private static final Map<String, String> NO_PROPS = Map.of();

    private static final String INVALID_A = "quality/invalid/DuplicateMethodBodyInvalidA.java";
    private static final String INVALID_B = "quality/invalid/DuplicateMethodBodyInvalidB.java";
    private static final String VALID = "quality/valid/DuplicateMethodBodyValid.java";

    private static final int CROSS_FILE_DUPLICATES = 3;
    private static final int LOOSER_MIN_STATEMENTS = 3;
    private static final int TIGHTER_MAX_BODY_NODES = 50;
    private static final Map<String, String> LOOSER_PROPS = Map.of("minStatements", "3");

    @Test
    void singleFileDetectsWithinClassDuplicate() throws Exception {
        List<AuditEvent> violations = runSingle(INVALID_A);
        assertEquals(1, violations.size(), format(violations));
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
        assertEquals(CROSS_FILE_DUPLICATES, violations.size(), format(violations));
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
    void asymptoticLargeMethodSkippedBySizeCap() throws Exception {
        assertTrue(runSingle("quality/invalid/DuplicateMethodBodyLarge.java").isEmpty());
    }

    @Test
    void settersAreDirectlyInvokable() {
        DuplicateMethodBodyCheck check = new DuplicateMethodBodyCheck();
        check.setMinStatements(LOOSER_MIN_STATEMENTS);
        check.setMaxBodyNodes(TIGHTER_MAX_BODY_NODES);
        assertNotNull(check);
    }

    @Test
    void tunedThresholdsRespected() throws Exception {
        List<AuditEvent> violations = TestCheckSupportUtil.runTreeWalkerCheck(DuplicateMethodBodyCheck.class, INVALID_A, LOOSER_PROPS);
        assertTrue(violations.stream().noneMatch(v -> v.getMessage().contains("walkBorNode")), "2-statement methods skipped: " + format(violations));
    }

    private static List<AuditEvent> runSingle(String resource) throws Exception {
        return TestCheckSupportUtil.runTreeWalkerCheck(DuplicateMethodBodyCheck.class, resource, NO_PROPS);
    }

    private static List<AuditEvent> runMulti(String... resources) throws Exception {
        return TestCheckSupportUtil.runTreeWalkerCheckMultiFile(DuplicateMethodBodyCheck.class, List.of(resources), NO_PROPS);
    }

    private static String format(List<AuditEvent> events) {
        return events.stream()
            .map(e -> "\n  Line " + e.getLine() + " [" + e.getFileName() + "]: " + e.getMessage())
            .toList()
            .toString();
    }
}
