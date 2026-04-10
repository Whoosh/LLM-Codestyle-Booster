package io.github.llmcodestyle.simplify;

import io.github.llmcodestyle.TestCheckSupport;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RedundantConstantAliasCheckTest {

    private static final Map<String, String> NO_PROPS = Map.of();
    private static final int EXPECTED_VIOLATIONS = 5;

    @Test
    void aliasesAndDuplicatePatternsAreFlagged() throws Exception {
        assertEquals(EXPECTED_VIOLATIONS, run("simplify/invalid/RedundantConstantAliasInvalid.java").size());
    }

    @Test
    void uniqueConstantsAndOneOffPatternsProduceNoViolations() throws Exception {
        assertTrue(run("simplify/valid/RedundantConstantAliasValid.java").isEmpty());
    }

    private static List<AuditEvent> run(String resource) throws Exception {
        return TestCheckSupport.runTreeWalkerCheck(RedundantConstantAliasCheck.class, resource, NO_PROPS);
    }
}
