package io.github.llmcodestyle;

import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.TreeWalker;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Shared helper for building Checker instances in custom check tests.
 */
public final class TestCheckSupport {

    private TestCheckSupport() {
    }

    /**
     * Run a TreeWalker check against a resource file and return violations.
     */
    public static List<AuditEvent> runTreeWalkerCheck(Class<?> checkClass, String resourceFile, Map<String, String> props) throws Exception {
        URL resource = TestCheckSupport.class.getClassLoader().getResource(resourceFile);
        assertNotNull(resource, "Test resource not found: " + resourceFile);

        DefaultConfiguration checkConfig = new DefaultConfiguration(checkClass.getName());
        for (Map.Entry<String, String> entry : props.entrySet()) {
            checkConfig.addProperty(entry.getKey(), entry.getValue());
        }

        DefaultConfiguration twConfig = new DefaultConfiguration(TreeWalker.class.getName());
        twConfig.addChild(checkConfig);

        DefaultConfiguration rootConfig = new DefaultConfiguration("root");
        rootConfig.addProperty("charset", "UTF-8");
        rootConfig.addChild(twConfig);

        Checker checker = new Checker();
        checker.setModuleClassLoader(TestCheckSupport.class.getClassLoader());
        checker.configure(rootConfig);

        List<AuditEvent> violations = new ArrayList<>();
        checker.addListener(new TestAuditListener(violations));
        checker.process(List.of(new File(resource.toURI())));
        checker.destroy();
        return violations;
    }

    /**
     * Run multiple TreeWalker checks simultaneously against a resource file.
     */
    public static List<AuditEvent> runMultipleTreeWalkerChecks(Map<String, Map<String, String>> checks, String resourceFile) throws Exception {
        URL resource = TestCheckSupport.class.getClassLoader().getResource(resourceFile);
        assertNotNull(resource, "Test resource not found: " + resourceFile);

        DefaultConfiguration twConfig = new DefaultConfiguration(TreeWalker.class.getName());
        for (Map.Entry<String, Map<String, String>> entry : checks.entrySet()) {
            DefaultConfiguration checkConfig = new DefaultConfiguration(entry.getKey());
            for (Map.Entry<String, String> prop : entry.getValue().entrySet()) {
                checkConfig.addProperty(prop.getKey(), prop.getValue());
            }
            twConfig.addChild(checkConfig);
        }

        DefaultConfiguration rootConfig = new DefaultConfiguration("root");
        rootConfig.addProperty("charset", "UTF-8");
        rootConfig.addChild(twConfig);

        Checker checker = new Checker();
        checker.setModuleClassLoader(TestCheckSupport.class.getClassLoader());
        checker.configure(rootConfig);

        List<AuditEvent> violations = new ArrayList<>();
        checker.addListener(new TestAuditListener(violations));
        checker.process(List.of(new File(resource.toURI())));
        checker.destroy();
        return violations;
    }

    /**
     * Run a FileSet check (AbstractFileSetCheck subclass) against a resource file.
     */
    public static List<AuditEvent> runFileSetCheck(Class<?> checkClass, String resourceFile, Map<String, String> props) throws Exception {
        URL resource = TestCheckSupport.class.getClassLoader().getResource(resourceFile);
        assertNotNull(resource, "Test resource not found: " + resourceFile);

        DefaultConfiguration checkConfig = new DefaultConfiguration(checkClass.getName());
        for (Map.Entry<String, String> entry : props.entrySet()) {
            checkConfig.addProperty(entry.getKey(), entry.getValue());
        }

        DefaultConfiguration rootConfig = new DefaultConfiguration("root");
        rootConfig.addProperty("charset", "UTF-8");
        rootConfig.addChild(checkConfig);

        Checker checker = new Checker();
        checker.setModuleClassLoader(TestCheckSupport.class.getClassLoader());
        checker.configure(rootConfig);

        List<AuditEvent> violations = new ArrayList<>();
        checker.addListener(new TestAuditListener(violations));
        checker.process(List.of(new File(resource.toURI())));
        checker.destroy();
        return violations;
    }
}
