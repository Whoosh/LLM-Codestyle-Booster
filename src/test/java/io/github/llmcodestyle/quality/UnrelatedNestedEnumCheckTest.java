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
        assertEquals(EXPECTED_VIOLATIONS, run("quality/invalid/UnrelatedNestedEnumInvalid.java").size());
    }

    @Test
    void enumsThatReferenceOuterAreIgnored() throws Exception {
        assertTrue(run("quality/valid/UnrelatedNestedEnumValid.java").isEmpty());
    }

    @Test
    void topLevelEnumIsIgnored() throws Exception {
        assertTrue(run("quality/valid/UnrelatedNestedEnumTopLevel.java").isEmpty());
    }

    private static List<AuditEvent> run(String resource) throws Exception {
        return runTreeWalkerCheck(UnrelatedNestedEnumCheck.class, resource, NO_PROPS);
    }
}
