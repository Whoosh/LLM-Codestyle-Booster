package io.github.llmcodestyle.quality;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.github.llmcodestyle.utils.TestCheckSupportUtil.*;
import static org.junit.jupiter.api.Assertions.*;

class UnusedPrivateMembersCheckTest {

    private static final int EXPECTED_VIOLATIONS = 2;
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
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("unusedField")),
            "Should report unused field name: " + format(violations));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("unusedMethod")),
            "Should report unused method name: " + format(violations));
    }

    @Test
    void edgeCasesProduceCorrectViolations() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(UnusedPrivateMembersCheck.class,
            "quality/invalid/UnusedPrivateMembersEdgeCases.java", NO_PROPS);
        // neverUsed, neverCalled, UnusedInner, deprecatedUnused should be flagged
        assertTrue(violations.size() >= 3, "Expected at least 3 violations for unused members: " + format(violations));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("neverUsed")),
            "Should flag neverUsed field: " + format(violations));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("neverCalled")),
            "Should flag neverCalled method: " + format(violations));
    }

    @Test
    void deprecatedPrivateMethodIsStillFlagged() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(UnusedPrivateMembersCheck.class,
            "quality/invalid/UnusedPrivateMembersEdgeCases.java", NO_PROPS);
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("deprecatedUnused")),
            "Deprecated private unused method should be flagged (only @Override exempts): " + format(violations));
    }

    @Test
    void serialVersionUIDIsNeverFlagged() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(UnusedPrivateMembersCheck.class,
            "quality/invalid/UnusedPrivateMembersEdgeCases.java", NO_PROPS);
        assertTrue(violations.stream().noneMatch(v -> v.getMessage().contains("serialVersionUID")),
            "serialVersionUID should never be flagged: " + format(violations));
    }

    @Test
    void overrideAnnotatedMethodIsNotFlagged() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(UnusedPrivateMembersCheck.class,
            "quality/invalid/UnusedPrivateMembersEdgeCases.java", NO_PROPS);
        assertTrue(violations.stream().noneMatch(v -> v.getMessage().contains("toString")),
            "Override-annotated methods should not be flagged: " + format(violations));
    }
}
