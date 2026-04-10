package io.github.llmcodestyle.quality;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.github.llmcodestyle.utils.TestCheckSupportUtil.*;
import static org.junit.jupiter.api.Assertions.*;

class UtilClassNamingCheckTest {

    private static final Map<String, String> NO_PROPS = Map.of();
    private static final int EXPECTED_VIOLATIONS = 3;

    @Test
    void utilShapedAndConstantsViolationsAreFlagged() throws Exception {
        assertEquals(EXPECTED_VIOLATIONS, run("quality/invalid/UtilClassNamingInvalid.java").size());
    }

    @Test
    void wellNamedAndMixedClassesAreNotFlagged() throws Exception {
        assertTrue(run("quality/valid/UtilClassNamingValid.java").isEmpty());
    }

    private static List<AuditEvent> run(String resource) throws Exception {
        return runTreeWalkerCheck(UtilClassNamingCheck.class, resource, NO_PROPS);
    }
}
