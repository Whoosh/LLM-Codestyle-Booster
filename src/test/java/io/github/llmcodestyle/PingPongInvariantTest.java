package io.github.llmcodestyle;

import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.TreeWalker;
import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import io.github.llmcodestyle.utils.TestAuditListener;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.github.llmcodestyle.PingPongInvariantTest.FixtureScope.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.DynamicTest.*;

/**
 * Verifies that the bundled Checkstyle configuration is "ping-pong free": running the full
 * set of custom checks against the canonical "fixed" form of any pattern (the {@code valid/}
 * fixtures, the curated {@code IdempotencyGolden*} files, and the project's own production
 * source tree) must produce zero violations.
 *
 * <p>If this invariant fails, fixing one check would trigger another check, and consumers
 * of this library would oscillate between the two suggestions forever.
 *
 * <h3>Three independent invariants</h3>
 * <ol>
 *   <li>{@link #allCustomChecksProduceZeroViolationsOnGoldenMain()} and {@link
 *       #allCustomChecksProduceZeroViolationsOnGoldenTest()} — the curated golden corpus is
 *       a fixed point under all custom checks.</li>
 *   <li>{@link #everyValidFixtureSurvivesEveryCustomCheck()} — every {@code valid/} fixture
 *       is the canonical fix form for one check; running every OTHER check against it must
 *       not fire (with scope filtering).</li>
 *   <li>{@link #productionSourceTreeIsPingPongFree()} — the project's own source tree is a
 *       fixed point.</li>
 * </ol>
 */
class PingPongInvariantTest {

    private static final Path SOURCE_ROOT = Path.of("src/main/java");
    private static final Path PRODUCTION_SOURCE = SOURCE_ROOT.resolve("io/github/llmcodestyle");
    private static final Path FIXTURE_RESOURCES = Path.of("src/test/resources");
    private static final String JAVA_EXT = ".java";

    private static final Pattern JUNIT_TEST_ANNOTATION = Pattern.compile("(?m)^\\s*@(org\\.junit\\.jupiter\\.api\\.)?Test\\b");

    /**
     * Per-package fixture roots scanned by the matrix. The {@code src/test/resources/valid/}
     * directory (AsymptoticSafetyTraps, ChainStressTest, Golden corpus) is intentionally NOT
     * scanned: stress fixtures deliberately violate ordinary checks; the Golden corpus is
     * exercised by the two dedicated golden tests.
     */
    private static final List<String> VALID_FIXTURE_DIRS = List.of("forbidden/valid", "layout/valid", "quality/valid", "simplify/valid");

    /**
     * Per-fixture scope-conflict map: each entry pairs a fixture filename with the set of
     * checks whose semantics inherently conflict with that fixture's test purpose. NOT
     * ping-pongs — the named check correctly fires under default config, but the fixture
     * exists specifically to exercise a non-default code shape.
     */
    private static final Map<String, Set<String>> FIXTURE_CHECK_EXCEPTIONS = Map.of(
        "TopLevelRecordInPojosPackageCustomSuffix.java",
            Set.of("io.github.llmcodestyle.quality.checks.TopLevelRecordInPojosPackageCheck"),
        "TopLevelEnumInEnumsPackageCustomSuffix.java",
            Set.of("io.github.llmcodestyle.quality.checks.TopLevelEnumInEnumsPackageCheck"),
        "UnrelatedNestedEnumTopLevel.java",
            Set.of("io.github.llmcodestyle.quality.checks.TopLevelEnumInEnumsPackageCheck"),
        "UnrelatedNestedRecordTopLevel.java",
            Set.of("io.github.llmcodestyle.quality.checks.TopLevelRecordInPojosPackageCheck"));

    /**
     * Checks that fire only in main scope; filtered out for test-scope fixtures.
     */
    private static final Set<String> MAIN_ONLY = Set.of("io.github.llmcodestyle.forbidden.NoSystemOutInProductionCheck");

    /**
     * Checks that fire only in test scope; filtered out for main-scope fixtures.
     */
    private static final Set<String> TEST_ONLY = Set.of(
        "io.github.llmcodestyle.quality.checks.TestClassNamingCheck",
        "io.github.llmcodestyle.quality.checks.TestMethodNameCheck",
        "io.github.llmcodestyle.quality.checks.LongTestLiteralCheck");

    /**
     * Checks that fundamentally cannot be tested via single-file fixtures. Each excluded
     * check is still active in production via {@code mvn verify -Pself-check}.
     */
    private static final Map<String, String> EXCLUDED_FROM_MATRIX = Map.ofEntries(
        Map.entry("io.github.llmcodestyle.quality.checks.PublicMethodTestCoverageCheck",
            "Requires test files to exist on the filesystem."),
        Map.entry("io.github.llmcodestyle.quality.checks.DuplicateMethodBodyCheck",
            "Cross-file structural similarity — fixtures intentionally have similar scenarios."),
        Map.entry("io.github.llmcodestyle.quality.checks.UnusedPrivateMembersCheck",
            "Fixtures contain private members as test data."),
        Map.entry("io.github.llmcodestyle.quality.checks.MethodMayBeStaticCheck",
            "Fixtures have minimal instance methods that legitimately don't reference this/super."),
        Map.entry("io.github.llmcodestyle.quality.checks.TestOnlyDelegateCheck",
            "Delegate detection requires call sites that exist in test code."),
        Map.entry("io.github.llmcodestyle.quality.checks.UnrelatedNestedClassCheck",
            "Fixtures use nested types as test scaffolding."),
        Map.entry("io.github.llmcodestyle.quality.checks.UnrelatedNestedEnumCheck",
            "Same as UnrelatedNestedClassCheck."),
        Map.entry("io.github.llmcodestyle.quality.checks.UnrelatedNestedInterfaceCheck",
            "Same as UnrelatedNestedClassCheck."),
        Map.entry("io.github.llmcodestyle.quality.checks.UnrelatedNestedRecordCheck",
            "Same as UnrelatedNestedClassCheck."),
        Map.entry("io.github.llmcodestyle.quality.checks.PackageGroupingByClassSuffixCheck",
            "Operates on directory-level class layout; fixture directories intentionally mix many *Valid / *Killer suffixes by design."),
        Map.entry("io.github.llmcodestyle.simplify.StaticImportCandidateCheck",
            "Suggestion-only check (not a transformation)."));

    /**
     * Per-check property overrides matching the bundled checkstyle.xml. Any check not listed here uses defaults.
     */
    private static final Map<String, Map<String, String>> CHECK_PROPS = Map.of(
        "io.github.llmcodestyle.layout.UnnecessaryLineWrapCheck",
            Map.of("maxLineLength", "180"),
        "io.github.llmcodestyle.layout.ChainedCallLineBreakCheck",
            Map.of("minChainLength", "4"),
        "io.github.llmcodestyle.layout.CompactableParameterListCheck",
            Map.of("maxLineLength", "180"),
        "io.github.llmcodestyle.quality.checks.LongTestLiteralCheck",
            Map.of("maxLength", "80"));

    enum FixtureScope {

        MAIN, TEST;

        boolean isExcluded(String fqn) {
            return this == TEST && MAIN_ONLY.contains(fqn) || this == MAIN && TEST_ONLY.contains(fqn);
        }
    }

    @Test
    void allCustomChecksProduceZeroViolationsOnGoldenMain() throws Exception {
        Path golden = FIXTURE_RESOURCES.resolve("valid/IdempotencyGoldenMain.java");
        assertTrue(Files.exists(golden), "Golden main fixture missing: " + golden);
        assertNoCheckFiresOn(golden, discoverCustomTreeWalkerChecks());
    }

    @Test
    void allCustomChecksProduceZeroViolationsOnGoldenTest() throws Exception {
        Path golden = FIXTURE_RESOURCES.resolve("valid/IdempotencyGoldenTest.java");
        assertTrue(Files.exists(golden), "Golden test fixture missing: " + golden);
        assertNoCheckFiresOn(golden, discoverCustomTreeWalkerChecks());
    }

    @TestFactory
    Stream<DynamicTest> everyValidFixtureSurvivesEveryCustomCheck() throws Exception {
        List<Class<?>> checks = discoverCustomTreeWalkerChecks();
        List<Path> fixtures = discoverValidFixtures();
        assertFalse(checks.isEmpty(), "no custom TreeWalker checks discovered under " + PRODUCTION_SOURCE);
        assertFalse(fixtures.isEmpty(), "no valid fixtures discovered under " + FIXTURE_RESOURCES);
        return fixtures.stream().map(fixture -> dynamicTest(fixture.getFileName().toString() + " [" + scopeOf(fixture) + "]", () -> assertNoCheckFiresOn(fixture, checks)));
    }

    @Test
    void productionSourceTreeIsPingPongFree() throws Exception {
        List<Class<?>> checks = discoverCustomTreeWalkerChecks();
        List<File> sources = discoverProductionSources();
        assertFalse(sources.isEmpty(), "no production sources discovered under " + PRODUCTION_SOURCE);

        Map<String, Map<String, String>> applicable = new HashMap<>();
        for (Class<?> check : checks) {
            String fqn = check.getName();
            if (EXCLUDED_FROM_MATRIX.containsKey(fqn) || TEST_ONLY.contains(fqn)) {
                continue;
            }
            applicable.put(fqn, CHECK_PROPS.getOrDefault(fqn, Map.of()));
        }

        Checker checker = buildMultiCheckChecker(applicable);
        try {
            List<AuditEvent> violations = process(checker, sources);
            assertTrue(violations.isEmpty(), "Production source tree must be a fixed point:\n" + formatViolations(violations));
        } finally {
            checker.destroy();
        }
    }

    private static void assertNoCheckFiresOn(Path fixture, List<Class<?>> allChecks) throws Exception {
        FixtureScope scope = scopeOf(fixture);
        Set<String> fixtureExceptions = FIXTURE_CHECK_EXCEPTIONS.getOrDefault(fixture.getFileName().toString(), Set.of());
        Map<String, Map<String, String>> applicable = new HashMap<>();
        for (Class<?> check : allChecks) {
            String fqn = check.getName();
            if (EXCLUDED_FROM_MATRIX.containsKey(fqn) || fixtureExceptions.contains(fqn) || scope.isExcluded(fqn)) {
                continue;
            }
            applicable.put(fqn, CHECK_PROPS.getOrDefault(fqn, Map.of()));
        }
        Checker checker = buildMultiCheckChecker(applicable);
        try {
            List<AuditEvent> violations = process(checker, List.of(fixture.toFile()));
            assertTrue(violations.isEmpty(), "Ping-pong: fix form '" + fixture.getFileName() + "' (" + scope + " scope) triggers checks:\n" + formatViolations(violations));
        } finally {
            checker.destroy();
        }
    }

    private static Checker buildMultiCheckChecker(Map<String, Map<String, String>> checks) throws Exception {
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
        checker.setModuleClassLoader(PingPongInvariantTest.class.getClassLoader());
        checker.configure(rootConfig);
        return checker;
    }

    private static List<AuditEvent> process(Checker checker, List<File> files) throws Exception {
        List<AuditEvent> violations = new ArrayList<>();
        checker.addListener(new TestAuditListener(violations));
        checker.process(files);
        return violations;
    }

    private static List<Class<?>> discoverCustomTreeWalkerChecks() throws Exception {
        try (Stream<Path> walk = Files.walk(PRODUCTION_SOURCE)) {
            List<Path> sourceFiles = walk
                .filter(Files::isRegularFile)
                .filter(p -> p.getFileName().toString().endsWith("Check.java"))
                .sorted()
                .toList();
            List<Class<?>> classes = new ArrayList<>();
            for (Path source : sourceFiles) {
                Class<?> loaded = loadCheckClass(source);
                if (AbstractCheck.class.isAssignableFrom(loaded)) {
                    classes.add(loaded);
                }
            }
            return classes;
        }
    }

    private static List<Path> discoverValidFixtures() throws IOException {
        List<Path> fixtures = new ArrayList<>();
        for (String dir : VALID_FIXTURE_DIRS) {
            Path root = FIXTURE_RESOURCES.resolve(dir);
            if (!Files.isDirectory(root)) {
                continue;
            }
            try (Stream<Path> stream = Files.list(root)) {
                stream
                    .filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().endsWith(JAVA_EXT))
                    .sorted()
                    .forEach(fixtures::add);
            }
        }
        return fixtures;
    }

    private static List<File> discoverProductionSources() throws IOException {
        try (Stream<Path> walk = Files.walk(PRODUCTION_SOURCE)) {
            return walk
                .filter(Files::isRegularFile)
                .filter(p -> p.getFileName().toString().endsWith(JAVA_EXT))
                .sorted()
                .map(Path::toFile)
                .toList();
        }
    }

    private static Class<?> loadCheckClass(Path source) {
        String relative = SOURCE_ROOT.relativize(source).toString().replace(File.separatorChar, '.');
        String fqn = relative.substring(0, relative.length() - JAVA_EXT.length());
        try {
            return Class.forName(fqn);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Could not load check class " + fqn, e);
        }
    }

    private static FixtureScope scopeOf(Path fixture) {
        if (isTestByName(fixture.getFileName().toString())) {
            return TEST;
        }
        try {
            if (JUNIT_TEST_ANNOTATION.matcher(Files.readString(fixture)).find()) {
                return TEST;
            }
        } catch (IOException ignored) {
            // Fall through to MAIN — failure to read content is reported by the actual check run.
        }
        return MAIN;
    }

    private static boolean isTestByName(String name) {
        if (name.contains("TestClass") || name.contains("TestMethod") || name.contains("TestSlow")) {
            return true;
        }
        return name.endsWith("Test.java") || name.startsWith("LongTestLiteral") || name.startsWith("TestClassNaming") || name.startsWith("TestMethodName");
    }

    private static String formatViolations(List<AuditEvent> violations) {
        if (violations.isEmpty()) {
            return "(none)";
        }
        return violations.stream()
            .sorted(Comparator.comparing(AuditEvent::getFileName).thenComparingInt(AuditEvent::getLine))
            .map(v -> "  " + new File(v.getFileName()).getName() + ":" + v.getLine() + " — "
                + shortName(v.getSourceName()) + " — " + v.getMessage())
            .collect(Collectors.joining("\n"));
    }

    private static String shortName(String fqn) {
        int dot = fqn.lastIndexOf('.');
        return dot < 0 ? fqn : fqn.substring(dot + 1);
    }
}
