# LLM Codestyle Booster

Custom Checkstyle checks + bundled PMD / SpotBugs / Checkstyle configs.
One dependency brings all code-quality rules to any Maven project.

## Build & verify

```bash
mvn verify                    # build + all static analysis + tests
mvn test                      # unit tests only (excludes *SlowTest.java)
```

### Self-check with custom rules (avoids circular dependency)

```bash
mvn install -DskipTests -Dpmd.skip -Dcpd.skip -Dspotbugs.skip -Dcheckstyle.skip
mvn verify -Pself-check
```

## Project structure

```
src/main/java/io/github/llmcodestyle/
  forbidden/   — checks that ban dangerous patterns (System.out, assert, generic catch, suppressions, commented-out code, unicode escapes)
  layout/      — formatting rules (line wrap, chained calls, parameter lists, static-final order, blank lines after comments, array init spacing, static star imports)
  quality/     — code quality & test hygiene (test naming, coverage, unused members, util packaging, long literals, test-only delegates, repeated exception wrapping, duplicate regex constants, Spring Boot main visibility, unrelated nested records)
  simplify/    — simplification suggestions (indexOf→contains, isEmpty, inline regex, single-use vars, collapsible concatenation, static import candidates, identical catch bodies, containsKey+get, Collections→List.of, conditional return→ternary, collapsible guard clauses, collapsible nested ifs, boolean-from-condition, split decl/assign, if-return boolean literal, redundant constant alias)
  utils/       — shared AST utilities (AstUtil, AstMethodCallUtil, AstAnnotationUtil, AstSingleUseUtil)

src/main/resources/io/github/llmcodestyle/
  config/
    checkstyle.xml          — full Checkstyle config with all 42 custom + built-in checks
    pmd-ruleset.xml         — paranoid-mode PMD 7 ruleset
    spotbugs-exclude.xml    — SpotBugs/fb-contrib/findsecbugs exclusions
  packagenames.xml          — registers io.github.llmcodestyle for Checkstyle module lookup
  forbidden/messages.properties
  layout/messages.properties
  quality/messages.properties
  simplify/messages.properties

src/test/
  java/    — one test class per check + integration tests (CheckstyleConfigConsistencyTest, CrossAnalyzerConsistencyTest, AsymptoticSafetyTest)
  resources/
    forbidden/valid/  forbidden/invalid/  — test data for forbidden checks
    layout/valid/     layout/invalid/     — test data for layout checks
    quality/valid/    quality/invalid/    — test data for quality checks
    simplify/valid/   simplify/invalid/   — test data for simplify checks
    valid/            — shared cross-cutting fixtures (IdempotencyGolden*, AsymptoticSafetyTraps)
    coverage/         — test data for PublicMethodTestCoverageCheck
```

## How checks are registered

Checkstyle discovers custom checks via `packagenames.xml` on the classpath.
When a consumer adds this jar as a Checkstyle dependency, all checks under
`io.github.llmcodestyle.*` become available by fully-qualified name.

## Adding a new check

1. Pick the right package: `forbidden`, `layout`, `quality`, or `simplify`.
2. Create a class extending `AbstractCheck` (TreeWalker) or `AbstractFileSetCheck` (file-level).
3. Add error messages to the package's `messages.properties` file.
4. Add the check to `src/main/resources/io/github/llmcodestyle/config/checkstyle.xml`.
5. Write tests:
   - Create test input files in `src/test/resources/<package>/valid/` and `src/test/resources/<package>/invalid/` (e.g. `layout/valid/`, `forbidden/invalid/`).
   - Write a test class using `TestCheckSupport` helper.
6. Register in `CheckstyleConfigConsistencyTest.java` comment registry (the `CrossAnalyzerConsistencyTest` enforces this).
7. Run `mvn verify` — JaCoCo enforces 85% line coverage.

## Conventions

- **Commit messages**: always in English.
- **No GodClass suppression for Util classes**: if a utility class triggers PMD GodClass, split it into focused classes by responsibility (e.g. `AstUtil` for structure/types, `AstMethodCallUtil` for METHOD_CALL analysis, `AstAnnotationUtil` for annotations, `AstSingleUseUtil` for variable analysis).
- **Token arrays**: extract `new int[] {...}` from `getDefaultTokens()`/`getAcceptableTokens()`/`getRequiredTokens()` into `private static final int[] TOKENS` and return `TOKENS.clone()`.

## Key constraints

- **Java 25** — source and target.
- **No circular dependency**: the project's own checkstyle-self.xml uses only built-in rules. Full self-check with custom rules requires the `self-check` profile (see above).
- **Checkstyle 13.4.0** is the compile dependency version.
- **JaCoCo**: 85% line coverage minimum.
- Static analysis runs on `verify` phase: Checkstyle, PMD + CPD, SpotBugs (with fb-contrib + findsecbugs).

## Publishing

Published to GitHub Packages (https://github.com/Whoosh/LLM-Codestyle-Booster) via GitHub Actions on release creation.
The `publish.yml` workflow sets the version from the release tag automatically.

## Version conventions

- `main` branch uses `1.0-SNAPSHOT`.
- Releases are tagged as `1.0.0`, `1.1.0`, etc.
