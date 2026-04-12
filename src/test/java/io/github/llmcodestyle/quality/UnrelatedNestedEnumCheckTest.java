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

    private static List<AuditEvent> run(String resource) throws Exception {
        return runTreeWalkerCheck(UnrelatedNestedEnumCheck.class, resource, NO_PROPS);
    }
}
