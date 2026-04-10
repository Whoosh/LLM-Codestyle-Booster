package io.github.llmcodestyle.simplify;

import io.github.llmcodestyle.utils.TestCheckSupportUtil;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class CollectionsToListOfCheckTest {

    private static final Map<String, String> NO_PROPS = Map.of();
    private static final int EXPECTED_VIOLATIONS = 4;

    @Test
    void preJava9FactoryMethodsProduceViolations() throws Exception {
        assertEquals(EXPECTED_VIOLATIONS, runCheck("simplify/invalid/CollectionsToListOfInvalid.java").size());
    }

    @Test
    void java9FactoryMethodsProduceNoViolations() throws Exception {
        assertTrue(runCheck("simplify/valid/CollectionsToListOfValid.java").isEmpty());
    }

    private static List<AuditEvent> runCheck(String resource) throws Exception {
        return TestCheckSupportUtil.runTreeWalkerCheck(CollectionsToListOfCheck.class, resource, NO_PROPS);
    }
}
