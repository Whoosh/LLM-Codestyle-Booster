package io.github.llmcodestyle.layout;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.github.llmcodestyle.utils.TestCheckSupportUtil.*;
import static org.junit.jupiter.api.Assertions.*;

class StaticStarImportCheckTest {

    private static final Map<String, String> NO_PROPS = Map.of();
    private static final String INVALID = "layout/invalid/StaticStarImportInvalid.java";
    private static final int EXPECTED_VIOLATIONS = 3;

    @Test
    void explicitStaticImportsProduceViolations() throws Exception {
        List<AuditEvent> violations = runCheck(INVALID);
        assertEquals(EXPECTED_VIOLATIONS, violations.size(), "Expected 3 violations: " + format(violations));
    }

    @Test
    void starStaticImportsProduceNoViolations() throws Exception {
        assertTrue(runCheck("layout/valid/StaticStarImportValid.java").isEmpty());
    }

    @Test
    void collisionAllowsExplicitImports() throws Exception {
        assertTrue(runCheck("layout/valid/StaticStarImportCollisionValid.java").isEmpty());
    }

    @Test
    void violationsPointToImportLines() throws Exception {
        List<AuditEvent> violations = runCheck(INVALID);
        for (AuditEvent event : violations) {
            assertTrue(event.getLine() >= 3 && event.getLine() <= 5,
                "Violation should be on an import line (3-5), got line " + event.getLine());
        }
    }

    @Test
    void collisionWithSameClassDoesNotSuppressViolation() throws Exception {
        // The collision file has PI from two different classes, which IS a collision.
        // But the star import for Collections is still valid.
        List<AuditEvent> violations = runCheck("layout/valid/StaticStarImportCollisionValid.java");
        assertTrue(violations.isEmpty(), "Name collision should allow explicit imports: " + format(violations));
    }

    @Test
    void extractParentClassSingleIdentPath() throws Exception {
        // L105 NO_COVERAGE: `return parentDot.getText()`
        // Two-part static import `import static SomeClass.MEMBER;` — DOT's first child is IDENT, not DOT.
        List<AuditEvent> violations = runCheck("layout/invalid/StaticStarImportSingleIdent.java");
        assertEquals(1, violations.size(),
            "Two-part static import should be flagged: " + format(violations));
    }

    private static List<AuditEvent> runCheck(String resource) throws Exception {
        return runTreeWalkerCheck(StaticStarImportCheck.class, resource, NO_PROPS);
    }
}
