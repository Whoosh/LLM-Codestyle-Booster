package io.github.llmcodestyle.quality;

import io.github.llmcodestyle.utils.TestCheckSupportUtil;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MethodMayBeStaticCheckTest {

    private static final Map<String, String> NO_PROPS = Map.of();
    private static final int EXPECTED_VIOLATIONS = 5;

    @Test
    void privateMethodsWithoutInstanceStateAreFlagged() throws Exception {
        assertEquals(EXPECTED_VIOLATIONS, run("quality/invalid/MethodMayBeStaticInvalid.java").size());
    }

    @Test
    void instanceMethodsAndShadowingAndNestedNonStaticAreNotFlagged() throws Exception {
        assertTrue(run("quality/valid/MethodMayBeStaticValid.java").isEmpty());
    }

    private static List<AuditEvent> run(String resource) throws Exception {
        return TestCheckSupportUtil.runTreeWalkerCheck(MethodMayBeStaticCheck.class, resource, NO_PROPS);
    }
}
