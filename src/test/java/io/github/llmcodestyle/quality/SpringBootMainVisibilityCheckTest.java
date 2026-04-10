package io.github.llmcodestyle.quality;

import io.github.llmcodestyle.TestCheckSupport;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SpringBootMainVisibilityCheckTest {

    private static final Map<String, String> NO_PROPS = Map.of();

    @Test
    void packagePrivateMainProducesViolation() throws Exception {
        List<AuditEvent> violations = run("quality/invalid/SpringBootMainPackagePrivate.java");
        assertEquals(1, violations.size());
        assertTrue(violations.get(0).getMessage().contains("not public"));
    }

    @Test
    void protectedMainProducesViolation() throws Exception {
        List<AuditEvent> violations = run("quality/invalid/SpringBootMainProtected.java");
        assertEquals(1, violations.size());
        assertTrue(violations.get(0).getMessage().contains("not public"));
    }

    @Test
    void privateMainProducesViolation() throws Exception {
        List<AuditEvent> violations = run("quality/invalid/SpringBootMainPrivate.java");
        assertEquals(1, violations.size());
        assertTrue(violations.get(0).getMessage().contains("not public"));
    }

    @Test
    void notStaticMainProducesViolation() throws Exception {
        List<AuditEvent> violations = run("quality/invalid/SpringBootMainNotStatic.java");
        assertEquals(1, violations.size());
        assertTrue(violations.get(0).getMessage().contains("not static"));
    }

    @Test
    void privateNonStaticMainProducesTwoViolations() throws Exception {
        assertEquals(2, run("quality/invalid/SpringBootMainPrivateNonStatic.java").size());
    }

    @Test
    void missingMainProducesViolation() throws Exception {
        List<AuditEvent> violations = run("quality/invalid/SpringBootMainMissing.java");
        assertEquals(1, violations.size());
        assertTrue(violations.get(0).getMessage().contains("missing"));
    }

    @Test
    void publicStaticMainHasNoViolations() throws Exception {
        assertTrue(run("quality/valid/SpringBootMainPublic.java").isEmpty());
    }

    @Test
    void publicStaticVarargsMainHasNoViolations() throws Exception {
        assertTrue(run("quality/valid/SpringBootMainPublicVarargs.java").isEmpty());
    }

    @Test
    void notAnnotatedClassIsIgnored() throws Exception {
        assertTrue(run("quality/valid/SpringBootNotAnnotated.java").isEmpty());
    }

    @Test
    void qualifiedAnnotationIsRecognized() throws Exception {
        assertTrue(run("quality/valid/SpringBootMainQualifiedAnnotation.java").isEmpty());
    }

    private static List<AuditEvent> run(String resource) throws Exception {
        return TestCheckSupport.runTreeWalkerCheck(SpringBootMainVisibilityCheck.class, resource, NO_PROPS);
    }
}
