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
        assertEquals(EXPECTED_VIOLATIONS, runCheck(INVALID).size());
    }

    @Test
    void starStaticImportsProduceNoViolations() throws Exception {
        assertTrue(runCheck("layout/valid/StaticStarImportValid.java").isEmpty());
    }

    @Test
    void collisionAllowsExplicitImports() throws Exception {
        assertTrue(runCheck("layout/valid/StaticStarImportCollisionValid.java").isEmpty());
    }

    private static List<AuditEvent> runCheck(String resource) throws Exception {
        return runTreeWalkerCheck(StaticStarImportCheck.class, resource, NO_PROPS);
    }
}
