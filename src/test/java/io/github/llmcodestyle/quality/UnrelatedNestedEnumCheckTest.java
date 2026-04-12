package io.github.llmcodestyle.quality;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.github.llmcodestyle.utils.TestCheckSupportUtil.*;
import static org.junit.jupiter.api.Assertions.*;

class UnrelatedNestedEnumCheckTest {

    private static final Map<String, String> NO_PROPS = Map.of();
    private static final int EXPECTED_VIOLATIONS = 5;

    @Test
    void unrelatedNestedEnumsProduceViolations() throws Exception {
        List<AuditEvent> violations = run("quality/invalid/UnrelatedNestedEnumInvalid.java");
        assertEquals(EXPECTED_VIOLATIONS, violations.size(), "Expected 5 unrelated enum violations: " + format(violations));
    }

    @Test
    void enumsThatReferenceOuterAreIgnored() throws Exception {
        assertTrue(run("quality/valid/UnrelatedNestedEnumValid.java").isEmpty());
    }

    @Test
    void topLevelEnumIsIgnored() throws Exception {
        assertTrue(run("quality/valid/UnrelatedNestedEnumTopLevel.java").isEmpty());
    }

    @Test
    void violationMessageUsesCorrectKey() throws Exception {
        List<AuditEvent> violations = run("quality/invalid/UnrelatedNestedEnumInvalid.java");
        assertFalse(violations.isEmpty());
        for (AuditEvent event : violations) {
            String msg = event.getMessage();
            assertTrue(msg.contains("enum") || msg.contains("nested") || msg.contains("unrelated"),
                "Message should reference enum/nested/unrelated: " + msg);
        }
    }

    @Test
    void targetTokenReturnsEnumDef() {
        UnrelatedNestedEnumCheck check = new UnrelatedNestedEnumCheck();
        int[] tokens = check.getDefaultTokens();
        assertNotNull(tokens);
        assertTrue(tokens.length > 0, "Should have at least one token");
    }

    @Test
    void enumWithSameNamedLocalVarIsStillUnrelated() throws Exception {
        // Exercises collectOwnDeclaredNames and isIdentReference edge cases
        List<AuditEvent> violations = run("quality/invalid/UnrelatedNestedEnumMutationKiller.java");
        // Standalone enum declares 'name' as parameter, which shadows outer 'name' field
        // But the enum is still unrelated because it only references its own 'name'
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Standalone")),
            "Standalone enum with shadowed name should be flagged: " + format(violations));
    }

    @Test
    void violationCountForMutationKiller() throws Exception {
        List<AuditEvent> violations = run("quality/invalid/UnrelatedNestedEnumMutationKiller.java");
        assertEquals(1, violations.size(),
            "Expected exactly 1 violation for mutation killer: " + format(violations));
    }

    @Test
    void enumConstantMatchingOuterFieldStillFlagged() throws Exception {
        // collectEnumConstantNames adds constant names to own-names.
        // If broken, constants like "name" would not be filtered, and the
        // enum would falsely appear to reference the outer "name" field.
        List<AuditEvent> violations = run("quality/invalid/UnrelatedNestedEnumMutKiller2.java");
        assertEquals(1, violations.size(),
            "Enum with constant matching outer field should be flagged: " + format(violations));
        assertTrue(violations.get(0).getMessage().contains("Status"),
            "Should flag Status enum: " + format(violations));
    }

    private static List<AuditEvent> run(String resource) throws Exception {
        return runTreeWalkerCheck(UnrelatedNestedEnumCheck.class, resource, NO_PROPS);
    }
}
