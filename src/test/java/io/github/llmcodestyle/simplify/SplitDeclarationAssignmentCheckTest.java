package io.github.llmcodestyle.simplify;

import io.github.llmcodestyle.TestCheckSupport;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SplitDeclarationAssignmentCheckTest {

    private static final Map<String, String> NO_PROPS = Map.of();
    private static final int EXPECTED_VIOLATIONS = 8;

    @Test
    void splitDeclAssignmentProducesViolations() throws Exception {
        assertEquals(EXPECTED_VIOLATIONS, run("simplify/invalid/SplitDeclarationAssignmentInvalid.java").size());
    }

    @Test
    void validCornerCasesProduceNoViolations() throws Exception {
        assertTrue(run("simplify/valid/SplitDeclarationAssignmentValid.java").isEmpty());
    }

    private static List<AuditEvent> run(String resource) throws Exception {
        return TestCheckSupport.runTreeWalkerCheck(SplitDeclarationAssignmentCheck.class, resource, NO_PROPS);
    }
}
