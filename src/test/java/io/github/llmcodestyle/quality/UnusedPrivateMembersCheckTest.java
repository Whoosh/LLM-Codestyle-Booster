package io.github.llmcodestyle.quality;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.github.llmcodestyle.utils.TestCheckSupportUtil.*;
import static org.junit.jupiter.api.Assertions.*;

class UnusedPrivateMembersCheckTest {

    private static final int EXPECTED_VIOLATIONS = 2;
    private static final int MIN_EDGE_CASE_VIOLATIONS = 3;
    private static final Map<String, String> NO_PROPS = Map.of();

    @Test
    void invalidCasesProduceViolations() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(UnusedPrivateMembersCheck.class, "quality/invalid/UnusedPrivateMembersInvalid.java", NO_PROPS);
        assertEquals(EXPECTED_VIOLATIONS, violations.size(), "Expected 2 unused private member violations, got: " + format(violations));
    }

    @Test
    void validCasesProduceNoViolations() throws Exception {
        assertTrue(runTreeWalkerCheck(UnusedPrivateMembersCheck.class, "quality/valid/UnusedPrivateMembersValid.java", NO_PROPS).isEmpty(), "Expected no violations");
    }

    @Test
    void violationMessageContainsMemberName() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(UnusedPrivateMembersCheck.class, "quality/invalid/UnusedPrivateMembersInvalid.java", NO_PROPS);
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("unusedField")), "Should report unused field name: " + format(violations));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("unusedMethod")), "Should report unused method name: " + format(violations));
    }

    @Test
    void edgeCasesProduceCorrectViolations() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(UnusedPrivateMembersCheck.class, "quality/invalid/UnusedPrivateMembersEdgeCases.java", NO_PROPS);
        // neverUsed, neverCalled, UnusedInner, deprecatedUnused should be flagged
        assertTrue(violations.size() >= MIN_EDGE_CASE_VIOLATIONS, "Expected at least 3 violations for unused members: " + format(violations));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("neverUsed")), "Should flag neverUsed field: " + format(violations));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("neverCalled")), "Should flag neverCalled method: " + format(violations));
    }

    @Test
    void deprecatedPrivateMethodIsStillFlagged() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(UnusedPrivateMembersCheck.class, "quality/invalid/UnusedPrivateMembersEdgeCases.java", NO_PROPS);
        assertTrue(
            violations.stream().anyMatch(v -> v.getMessage().contains("deprecatedUnused")),
            "Deprecated private unused method should be flagged (only @Override exempts): " + format(violations));
    }

    @Test
    void serialVersionUIDIsNeverFlagged() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(UnusedPrivateMembersCheck.class, "quality/invalid/UnusedPrivateMembersEdgeCases.java", NO_PROPS);
        assertTrue(violations.stream().noneMatch(v -> v.getMessage().contains("serialVersionUID")), "serialVersionUID should never be flagged: " + format(violations));
    }

    @Test
    void overrideAnnotatedMethodIsNotFlagged() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(UnusedPrivateMembersCheck.class, "quality/invalid/UnusedPrivateMembersEdgeCases.java", NO_PROPS);
        assertTrue(violations.stream().noneMatch(v -> v.getMessage().contains("toString")), "Override-annotated methods should not be flagged: " + format(violations));
    }

    @Test
    void unusedMemberInNestedClassIsFlagged() throws Exception {
        // collectAllPrivateDeclarations lines 76-77: recursion into nested types
        List<AuditEvent> violations = runTreeWalkerCheck(UnusedPrivateMembersCheck.class, "quality/invalid/UnusedPrivateMembersNested.java", NO_PROPS);
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("nestedUnused")), "Unused private in nested class should be flagged: " + format(violations));
        // nestedUsed is used by getNestedUsed() — should NOT be flagged
        assertTrue(violations.stream().noneMatch(v -> v.getMessage().contains("nestedUsed")), "Used private in nested class should not be flagged: " + format(violations));
    }

    @Test
    void overrideInNestedIsNotFlagged() throws Exception {
        // isPrivateNonAnnotated lines 109-111: @Override detection
        List<AuditEvent> violations = runTreeWalkerCheck(UnusedPrivateMembersCheck.class, "quality/invalid/UnusedPrivateMembersNested.java", NO_PROPS);
        assertTrue(violations.stream().noneMatch(v -> v.getMessage().contains("toString")), "Override-annotated methods in nested should not be flagged: " + format(violations));
    }

    @Test
    void deprecatedAnnotationDoesNotExempt() throws Exception {
        // isPrivateNonAnnotated: only @Override exempts, not @Deprecated
        List<AuditEvent> violations = runTreeWalkerCheck(UnusedPrivateMembersCheck.class, "quality/invalid/UnusedPrivateMembersNested.java", NO_PROPS);
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("annotatedButNotOverride")), "Deprecated but not Override should be flagged: " + format(violations));
    }

    @Test
    void unusedPrivateRecordAndInterfaceAreFlagged() throws Exception {
        // PRIVATE_DECL_TOKENS must include RECORD_DEF and INTERFACE_DEF so that
        // unused nested private records/interfaces are reported just like classes.
        List<AuditEvent> violations = runTreeWalkerCheck(UnusedPrivateMembersCheck.class, "quality/invalid/UnusedPrivateMembersRecordsInterfaces.java", NO_PROPS);
        assertEquals(2, violations.size(), "Expected exactly 2 violations (UnusedPair, UnusedSpi), got: " + format(violations));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("UnusedPair")), "Should flag unused private record: " + format(violations));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("UnusedSpi")), "Should flag unused private interface: " + format(violations));
    }

    @Test
    void isPrivateNonAnnotatedAnnotationBranch() throws Exception {
        // L109 SURVIVED: `mod.getType() == ANNOTATION`
        // L111 SURVIVED: `annotIdent != null && "Override".equals(annotIdent.getText())`
        // Tests that @Override-annotated private methods are exempt, while @Deprecated private methods are NOT.
        List<AuditEvent> violations = runTreeWalkerCheck(UnusedPrivateMembersCheck.class, "quality/invalid/UnusedPrivateAnnotated.java", NO_PROPS);
        // unusedMethod: private, no annotation, unused -> flagged
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("unusedMethod")), "Unannotated unused private method should be flagged: " + format(violations));
        // deprecatedUnused: private, @Deprecated (not @Override), unused -> flagged
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("deprecatedUnused")), "Deprecated unused private should be flagged: " + format(violations));
        // overrideMethod: private, @Override -> NOT flagged (isPrivateNonAnnotated returns false)
        assertTrue(violations.stream().noneMatch(v -> v.getMessage().contains("overrideMethod")), "Override-annotated private should not be flagged: " + format(violations));
    }
}
