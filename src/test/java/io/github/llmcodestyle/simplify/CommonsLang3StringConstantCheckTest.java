package io.github.llmcodestyle.simplify;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.github.llmcodestyle.utils.TestCheckSupportUtil.*;
import static org.junit.jupiter.api.Assertions.*;

class CommonsLang3StringConstantCheckTest {

    private static final Map<String, String> NO_PROPS = Map.of();
    private static final int EXPECTED_VIOLATIONS = 4;

    @Test
    void emptySpaceLfCrConstantsAreFlagged() throws Exception {
        assertEquals(EXPECTED_VIOLATIONS, run("simplify/invalid/CommonsLang3StringConstantInvalid.java").size());
    }

    @Test
    void otherConstantsAndLocalsAreNotFlagged() throws Exception {
        assertTrue(run("simplify/valid/CommonsLang3StringConstantValid.java").isEmpty());
    }

    private static List<AuditEvent> run(String resource) throws Exception {
        return runTreeWalkerCheck(CommonsLang3StringConstantCheck.class, resource, NO_PROPS);
    }
}
