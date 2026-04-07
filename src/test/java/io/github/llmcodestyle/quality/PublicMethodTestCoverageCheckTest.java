package io.github.llmcodestyle.quality;

import io.github.llmcodestyle.TestAuditListener;

import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.TreeWalker;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import static java.nio.file.FileVisitResult.CONTINUE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PublicMethodTestCoverageCheckTest {

    private static final int TWO_VIOLATIONS = 2;

    private Path tempDir;

    @BeforeEach
    void setUp() throws Exception {
        tempDir = Files.createTempDirectory("checkstyle-coverage-test");
    }

    @AfterEach
    void tearDown() throws Exception {
        Files.walkFileTree(tempDir, new SimpleFileVisitor<>() {

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return CONTINUE;
            }
        });
    }

    @Test
    void untestedPublicMethodsProduceViolations() throws Exception {
        copyToTest("FooTest.java", "test/FooTest.java");
        List<AuditEvent> violations = runCheck(copyToMain("FooMain.java", "test/Foo.java"));
        assertEquals(TWO_VIOLATIONS, violations.size(), "untestedMethod + untestedStatic: " + format(violations));
    }

    @Test
    void allMethodsTestedProducesNoViolations() throws Exception {
        copyToTest("BarTest.java", "test/BarTest.java");
        List<AuditEvent> violations = runCheck(copyToMain("BarMain.java", "test/Bar.java"));
        assertTrue(violations.isEmpty(), "Unexpected violations: " + format(violations));
    }

    @Test
    void privateAndProtectedMethodsAreExcluded() throws Exception {
        copyToTest("PrivTest.java", "test/PrivTest.java");
        List<AuditEvent> violations = runCheck(copyToMain("PrivMain.java", "test/Priv.java"));
        assertTrue(violations.isEmpty(), "private/protected should be excluded: " + format(violations));
    }

    @Test
    void exemptMethodsAreExcluded() throws Exception {
        copyToTest("ExemptTest.java", "test/ExemptTest.java");
        List<AuditEvent> violations = runCheck(copyToMain("ExemptMain.java", "test/Exempt.java"));
        assertTrue(violations.isEmpty(), "exempt methods should be excluded: " + format(violations));
    }

    @Test
    void recordAccessorsAreExcluded() throws Exception {
        copyToTest("MyRecordTest.java", "test/MyRecordTest.java");
        List<AuditEvent> violations = runCheck(copyToMain("MyRecordMain.java", "test/MyRecord.java"));
        assertEquals(1, violations.size(), "customMethod only: " + format(violations));
    }

    @Test
    void slowTestFileAlsoCounts() throws Exception {
        copyToTest("SlowSlowTest.java", "test/SlowSlowTest.java");
        List<AuditEvent> violations = runCheck(copyToMain("SlowMain.java", "test/Slow.java"));
        assertTrue(violations.isEmpty(), "covered by SlowTest: " + format(violations));
    }

    @Test
    void noTestFileSkipsSilently() throws Exception {
        List<AuditEvent> violations = runCheck(copyToMain("OrphanMain.java", "test/Orphan.java"));
        assertTrue(violations.isEmpty(), "no test file should skip: " + format(violations));
    }

    @Test
    void packagePrivateMethodsAreChecked() throws Exception {
        copyToTest("PkgPrivTest.java", "test/PkgPrivTest.java");
        List<AuditEvent> violations = runCheck(copyToMain("PkgPrivMain.java", "test/PkgPriv.java"));
        assertEquals(1, violations.size(), "packageMethod not tested: " + format(violations));
    }

    @Test
    void abstractMethodsAreExcluded() throws Exception {
        copyToTest("BaseTest.java", "test/BaseTest.java");
        List<AuditEvent> violations = runCheck(copyToMain("BaseMain.java", "test/Base.java"));
        assertEquals(1, violations.size(), "concrete only, abstract excluded: " + format(violations));
    }

    @Test
    void sameNameMethodOnDifferentClassIsNotCounted() throws Exception {
        copyToTest("TargetTest.java", "test/TargetTest.java");
        List<AuditEvent> violations = runCheck(copyToMain("TargetMain.java", "test/Target.java"));
        assertEquals(1, violations.size(), "validate called on Other, not Target: " + format(violations));
    }

    @Test
    void methodReferenceCountsAsCoverage() throws Exception {
        copyToTest("MapperTest.java", "test/MapperTest.java");
        List<AuditEvent> violations = runCheck(copyToMain("MapperMain.java", "test/Mapper.java"));
        assertTrue(violations.isEmpty(), "method reference should count: " + format(violations));
    }

    @Test
    void varTypedVariableIsDetected() throws Exception {
        copyToTest("ServiceTest.java", "test/ServiceTest.java");
        List<AuditEvent> violations = runCheck(copyToMain("ServiceMain.java", "test/Service.java"));
        assertTrue(violations.isEmpty(), "var-typed variable should count: " + format(violations));
    }

    private Path copyToMain(String resourceName, String relativeDest) throws Exception {
        return copyResource("coverage/" + resourceName, "src/main/java/" + relativeDest);
    }

    private void copyToTest(String resourceName, String relativeDest) throws Exception {
        copyResource("coverage/" + resourceName, "src/test/java/" + relativeDest);
    }

    private Path copyResource(String resourcePath, String relativeDest) throws Exception {
        URL url = getClass().getClassLoader().getResource(resourcePath);
        if (url == null) {
            throw new IllegalStateException("Resource not found: " + resourcePath);
        }
        Path dest = tempDir.resolve(relativeDest);
        Files.createDirectories(dest.getParent());
        try (InputStream in = url.openStream()) {
            Files.copy(in, dest);
        }
        return dest;
    }

    private List<AuditEvent> runCheck(Path file) throws Exception {
        DefaultConfiguration checkConfig = new DefaultConfiguration(PublicMethodTestCoverageCheck.class.getName());
        DefaultConfiguration twConfig = new DefaultConfiguration(TreeWalker.class.getName());
        twConfig.addChild(checkConfig);
        DefaultConfiguration rootConfig = new DefaultConfiguration("root");
        rootConfig.addProperty("charset", "UTF-8");
        rootConfig.addChild(twConfig);
        Checker checker = new Checker();
        checker.setModuleClassLoader(getClass().getClassLoader());
        checker.configure(rootConfig);
        List<AuditEvent> violations = new ArrayList<>();
        checker.addListener(new TestAuditListener(violations));
        checker.process(List.of(file.toFile()));
        checker.destroy();
        return violations;
    }

    private static String format(List<AuditEvent> events) {
        return events.stream()
            .map(e -> "Line " + e.getLine() + ": " + e.getMessage())
            .toList()
            .toString();
    }
}
