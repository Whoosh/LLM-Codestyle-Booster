# LLM Codestyle Booster

One dependency that brings **50 custom Checkstyle checks** + fully configured **Checkstyle**, **PMD**, and **SpotBugs** rulesets to your Maven project.

Designed for teams that want strict, opinionated static analysis out of the box, with easy per-project overrides.

## What's inside

| Category | Checks | Examples |
|----------|--------|---------|
| **Forbidden** | 6 | No `System.out` in production, no `@SuppressWarnings`, no generic `catch(Exception)`, no commented-out code |
| **Layout** | 8 | Chained calls must break after 4+ dots, unnecessary line wraps, compactable parameter lists, array init spacing, static star imports |
| **Quality** | 14 | Test method naming, public method test coverage, unused private members, util class packaging & naming, may-be-static methods, duplicate regex constants, duplicate method bodies, Spring Boot main visibility, unrelated nested records |
| **Simplify** | 21 | `indexOf` &rarr; `contains`, `size() == 0` &rarr; `isEmpty()`, inline regex &rarr; `Pattern` constant, single-use variable inlining, identical catch bodies, `containsKey` + `get` &rarr; single lookup, collapsible guard clauses, nested ifs, consecutive ifs, or-chain &rarr; Set.contains, boolean-from-condition, split decl/assign, if-return literal, redundant constant alias, trivial single-use private methods, commons-lang3 string constants |

Plus bundled configs:
- **Checkstyle** &mdash; full config with all 47 custom checks + standard built-in checks
- **PMD** &mdash; paranoid-mode ruleset (all categories, strict thresholds)
- **SpotBugs** &mdash; max effort, low threshold, with fb-contrib and findsecbugs plugins

---

## Quick start

### 1. Add the GitHub Packages repository

In your project's `pom.xml` or `settings.xml`:

```xml
<repositories>
    <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/Whoosh/LLM-Codestyle-Booster</url>
    </repository>
</repositories>
```

GitHub Packages requires authentication. Add to your `~/.m2/settings.xml`:

```xml
<servers>
    <server>
        <id>github</id>
        <username>YOUR_GITHUB_USERNAME</username>
        <password>YOUR_GITHUB_TOKEN</password> <!-- token with read:packages scope -->
    </server>
</servers>
```

### 2. Configure Checkstyle

Add the plugin with `llm-codestyle-booster` as a dependency:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-checkstyle-plugin</artifactId>
    <version>3.6.0</version>
    <dependencies>
        <dependency>
            <groupId>com.puppycrawl.tools</groupId>
            <artifactId>checkstyle</artifactId>
            <version>13.4.0</version>
        </dependency>
        <dependency>
            <groupId>io.github.llmcodestyle</groupId>
            <artifactId>llm-codestyle-booster</artifactId>
            <version>1.0.0</version>
        </dependency>
    </dependencies>
    <configuration>
        <!-- Use the bundled config from the jar's classpath -->
        <configLocation>io/github/llmcodestyle/config/checkstyle.xml</configLocation>
        <consoleOutput>true</consoleOutput>
        <failsOnError>true</failsOnError>
        <includeTestSourceDirectory>true</includeTestSourceDirectory>
    </configuration>
    <executions>
        <execution>
            <id>checkstyle-check</id>
            <phase>verify</phase>
            <goals>
                <goal>check</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

### 3. Configure PMD

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-pmd-plugin</artifactId>
    <version>3.28.0</version>
    <dependencies>
        <dependency>
            <groupId>io.github.llmcodestyle</groupId>
            <artifactId>llm-codestyle-booster</artifactId>
            <version>1.0.0</version>
        </dependency>
    </dependencies>
    <configuration>
        <minimumTokens>50</minimumTokens>
        <printFailingErrors>true</printFailingErrors>
        <linkXRef>false</linkXRef>
        <rulesets>
            <!-- Use the bundled ruleset from the jar's classpath -->
            <ruleset>io/github/llmcodestyle/config/pmd-ruleset.xml</ruleset>
        </rulesets>
        <failOnViolation>true</failOnViolation>
    </configuration>
    <executions>
        <execution>
            <id>pmd-check</id>
            <phase>verify</phase>
            <goals><goal>check</goal></goals>
        </execution>
        <execution>
            <id>cpd-check</id>
            <phase>verify</phase>
            <goals><goal>cpd-check</goal></goals>
        </execution>
    </executions>
</plugin>
```

### 4. Configure SpotBugs

```xml
<plugin>
    <groupId>com.github.spotbugs</groupId>
    <artifactId>spotbugs-maven-plugin</artifactId>
    <version>4.9.8.3</version>
    <dependencies>
        <dependency>
            <groupId>io.github.llmcodestyle</groupId>
            <artifactId>llm-codestyle-booster</artifactId>
            <version>1.0.0</version>
        </dependency>
    </dependencies>
    <configuration>
        <effort>Max</effort>
        <threshold>Low</threshold>
        <xmlOutput>true</xmlOutput>
        <excludeFilterFile>io/github/llmcodestyle/config/spotbugs-exclude.xml</excludeFilterFile>
        <plugins>
            <plugin>
                <groupId>com.mebigfatguy.fb-contrib</groupId>
                <artifactId>fb-contrib</artifactId>
                <version>7.6.8</version>
            </plugin>
            <plugin>
                <groupId>com.h3xstream.findsecbugs</groupId>
                <artifactId>findsecbugs-plugin</artifactId>
                <version>1.13.0</version>
            </plugin>
        </plugins>
    </configuration>
    <executions>
        <execution>
            <id>spotbugs-check</id>
            <phase>verify</phase>
            <goals><goal>check</goal></goals>
        </execution>
    </executions>
</plugin>
```

---

## Suppressing checks in your project

### Checkstyle: suppress by file pattern

Create a `checkstyle-suppressions.xml` in your project:

```xml
<?xml version="1.0"?>
<!DOCTYPE suppressions PUBLIC
    "-//Checkstyle//DTD SuppressionFilter Configuration 1.2//EN"
    "https://checkstyle.org/dtds/suppressions_1_2.dtd">
<suppressions>
    <!-- Suppress a specific check for files matching a pattern -->
    <suppress checks="MagicNumber" files=".*Config\.java$"/>
    <suppress checks="MethodLength" files=".*Migration\.java$"/>

    <!-- Suppress a custom check entirely -->
    <suppress checks="CommentedOutCodeCheck" files=".*\.java$"/>

    <!-- Suppress by message pattern -->
    <suppress checks="RegexpSingleline" message="Util class must not have"/>
</suppressions>
```

Reference it in your Checkstyle plugin configuration:

```xml
<configuration>
    <configLocation>io/github/llmcodestyle/config/checkstyle.xml</configLocation>
    <suppressionsLocation>${project.basedir}/checkstyle-suppressions.xml</suppressionsLocation>
</configuration>
```

### Checkstyle: use your own config that extends the bundled one

If you need deeper customization, copy the bundled `checkstyle.xml` to your project and modify it. Reference your local copy:

```xml
<configuration>
    <configLocation>${project.basedir}/checkstyle.xml</configLocation>
</configuration>
```

### PMD: override rules

Create your own ruleset that references the bundled one and excludes specific rules:

```xml
<?xml version="1.0"?>
<ruleset name="My Project Rules"
         xmlns="http://pmd.sourceforge.net/ruleset/2.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://pmd.sourceforge.net/ruleset/2.0.0
         https://pmd.sourceforge.io/ruleset_2_0_0.xsd">

    <!-- Start with the bundled ruleset -->
    <rule ref="io/github/llmcodestyle/config/pmd-ruleset.xml">
        <!-- Exclude rules you don't want -->
        <exclude name="TooManyMethods"/>
        <exclude name="AvoidDuplicateLiterals"/>
    </rule>
</ruleset>
```

Then point to it:

```xml
<rulesets>
    <ruleset>${project.basedir}/pmd-ruleset.xml</ruleset>
</rulesets>
```

### PMD: suppress per-file or per-line

```java
@SuppressWarnings("PMD.TooManyMethods")  // suppress at class level
public class BigService {
    String name = ""; // NOPMD - false positive
}
```

> **Note**: the bundled Checkstyle config forbids `@SuppressWarnings` by default via `NoSuppressionCheck`. If you use the bundled Checkstyle config as-is, you'll need to suppress that check first (via suppressions XML) before using `@SuppressWarnings("PMD...")`.

### SpotBugs: add your own exclusions

Create a `spotbugs-exclude.xml` in your project:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<FindBugsFilter>
    <!-- Exclude a specific bug pattern from a package -->
    <Match>
        <Bug pattern="EI_EXPOSE_REP"/>
        <Package name="com.myapp.dto"/>
    </Match>

    <!-- Exclude a class entirely -->
    <Match>
        <Class name="com.myapp.legacy.OldService"/>
    </Match>
</FindBugsFilter>
```

Point to it instead of (or in addition to) the bundled one:

```xml
<configuration>
    <excludeFilterFile>${project.basedir}/spotbugs-exclude.xml</excludeFilterFile>
</configuration>
```

To use **both** bundled and local exclusions, list them comma-separated:

```xml
<excludeFilterFile>io/github/llmcodestyle/config/spotbugs-exclude.xml,${project.basedir}/spotbugs-exclude.xml</excludeFilterFile>
```

---

## All custom Checkstyle checks reference

### Forbidden

| Check | Description |
|-------|-------------|
| `CommentedOutCodeCheck` | Flags 2+ consecutive lines of commented-out Java code |
| `ForbidAssertKeywordCheck` | Forbids `assert` keyword; use Guava Preconditions or if-throw |
| `ForbiddenGenericCatchCheck` | Forbids catching `Exception`, `Throwable`, `RuntimeException` |
| `NoSuppressionCheck` | Forbids `@SuppressWarnings`, `@SuppressFBWarnings`, and inline NOPMD/CHECKSTYLE:OFF |
| `NoSystemOutInProductionCheck` | Forbids `System.out/err` in production code (exempts Main/Application classes) |
| `UnicodeEscapeCheck` | Forbids `\uXXXX` escapes (except control chars); use UTF-8 directly |

### Layout

| Check | Description |
|-------|-------------|
| `BlankLineAfterCommentCheck` | Flags blank lines between a comment and the code it describes |
| `ChainedCallLineBreakCheck` | Chains of 4+ method calls must break across lines |
| `CompactableParameterListCheck` | Flags multi-line parameter lists where continuation fits on previous line |
| `MethodCallArgumentsOnSameLineCheck` | Arguments: all-on-one-line or one-per-line, no mixed |
| `StaticFinalFirstCheck` | `static final` fields must be declared before instance fields and constructors |
| `ArrayInitSpaceCheck` | Requires a space between `]` and `{` in array initializers |
| `StaticStarImportCheck` | Requires wildcard (`*`) static imports unless name collision exists |
| `UnnecessaryLineWrapCheck` | Flags statements split across lines that fit within max line length |

### Quality

| Check | Description |
|-------|-------------|
| `LongTestLiteralCheck` | Flags long string literals (>80 chars) in test methods; extract to resource files |
| `PublicMethodTestCoverageCheck` | Flags public methods without corresponding test references |
| `TestClassNamingCheck` | Test classes must end in `Test` or `SlowTest` |
| `TestMethodNameCheck` | Test methods must be camelCase (no underscores) |
| `TestOnlyDelegateCheck` | Flags non-private methods that only delegate to a private method |
| `UnusedPrivateMembersCheck` | Flags private fields/methods/types never referenced |
| `UtilClassInUtilsPackageCheck` | `*Util` / `*Utils` classes must live in a `utils` package |
| `RepeatedExceptionWrappingCheck` | Flags 3+ catch blocks with identical catch-and-rethrow wrapping pattern |
| `DuplicateRegexConstantCheck` | Flags duplicate regex constants (`String`/`Pattern`) across classes in a module |
| `SpringBootMainVisibilityCheck` | Enforces `public static void main(String[])` on `@SpringBootApplication` classes |
| `UnrelatedNestedRecordCheck` | Flags nested records that don't reference the enclosing type — move to a `pojos` package |
| `MethodMayBeStaticCheck` | Flags `private` instance methods whose body never touches `this`/`super` or any instance member — add the `static` modifier |
| `UtilClassNamingCheck` | All-static-public classes must end in `*Util`/`*Utils`; `*Constants` classes must not declare any public method |
| `DuplicateMethodBodyCheck` | Cross-class detector for methods with structurally identical bodies after local/param renaming — consolidate into a shared helper |

### Simplify

| Check | Description |
|-------|-------------|
| `CollapsibleConstantConcatenationCheck` | `static final` string concatenation with `+` that could be a single literal |
| `IndexOfToContainsCheck` | `indexOf() < 0` &rarr; `!contains()` |
| `InlineRegexConstantCheck` | Inline regex in `matches()`/`split()` should be a `Pattern` constant |
| `PureSingleUseLocalVariableCheck` | Variable with pure initializer used once in a non-adjacent statement |
| `SingleUseLocalVariableCheck` | Variable assigned once and used once in immediately following statement |
| `StaticImportCandidateCheck` | Suggests static imports for frequently used `ClassName.CONSTANT` references |
| `UseIsEmptyCheck` | `size() == 0` / `length() > 0` &rarr; `isEmpty()` / `!isEmpty()` |
| `IdenticalCatchBodyCheck` | Flags catch blocks with identical bodies that can be merged via multi-catch |
| `MapContainsKeyThenGetCheck` | `if (map.containsKey(k)) map.get(k)` &rarr; single `getOrDefault`/`computeIfAbsent` |
| `CollectionsToListOfCheck` | `Collections.emptyList()` / `unmodifiableList(Arrays.asList(...))` &rarr; `List.of(...)` |
| `ConditionalReturnToTernaryCheck` | `if-else` with single return in each branch &rarr; ternary return |
| `CollapsibleGuardClauseCheck` | `if(a){return;} if(b){...}` at end of method &rarr; `if(!a && b){...}` |
| `CollapsibleNestedIfCheck` | `if(a){if(b){...}}` (no else) &rarr; `if(a && b){...}` |
| `CollapsibleConsecutiveIfCheck` | Consecutive `if(a){return X;} if(b){return X;}` with identical terminating bodies &rarr; `if(a || b){return X;}` |
| `OrChainToSetContainsCheck` | Chains of 3+ `expr == LITERAL` or `receiver.equals(LITERAL)` with shared LHS/receiver &rarr; `static final Set` + `contains()` |
| `BooleanFromConditionCheck` | `boolean x = false; if(cond) x = true;` &rarr; `boolean x = cond;` (and mirror) |
| `SplitDeclarationAssignmentCheck` | `int x; ...; x = 5;` with no use/branch between &rarr; merge into single declaration |
| `IfReturnBooleanLiteralCheck` | `if(c) return true; return false;` &rarr; `return c;` (no-else fall-through form) |
| `RedundantConstantAliasCheck` | Flags `static final A = B` aliases and same-class duplicate `Pattern.compile()` of the same regex |
| `TrivialSingleUsePrivateMethodCheck` | Flags `private` one-statement methods called from exactly one site — inline them; spares stream chains and methods that reuse a parameter |
| `CommonsLang3StringConstantCheck` | Flags `static final String` constants whose value matches `StringUtils.EMPTY` / `SPACE` / `LF` / `CR` from Apache Commons Lang3 |

---

## Fixing violations with an LLM — avoid cascade churn

If you let an LLM fix violations one at a time, you'll burn a lot of edit cycles:
the model extracts a long literal to a constant, which shifts the `static final`
ordering, which triggers an import reorder, which re-wraps a line, which exposes
a single-use local, and so on. Each pass resurfaces the same file because the
previous pass only handled one finding.

The checks themselves are deliberately independent (each is a self-contained
Checkstyle/PMD visitor), so atomicity has to come from the workflow. **Do this:**

1. Run the full suite once: `mvn verify` (or `mvn checkstyle:check pmd:check
   spotbugs:check` if you want to skip tests). This produces the authoritative
   list of everything wrong with the codebase in one shot.
2. **Group findings by file, not by rule.** The reports in
   `target/checkstyle-result.xml`, `target/pmd.xml`, and
   `target/spotbugsXml.xml` already include file paths and line numbers — a
   one-off `grep`/`jq`/`xmllint` is enough to regroup them.
3. Hand the per-file findings to the model as a **single prompt** with an
   explicit instruction: *"Apply every fix below to this file in a single edit.
   Do not commit after the first fix — many of these interact (line wrap ↔
   constant extraction ↔ import order ↔ static-final ordering)."*
4. Re-run `mvn verify`. Any remaining findings are genuinely new (cross-file
   effects or things the model missed), not cascade churn.

The key move is step 3: the model needs the **whole fix plan for the file**
before it starts editing, not one finding at a time. Otherwise each fix
invalidates the previous diff and you pay for the file twice.

### Rules that commonly interact

Keep these groups in mind when you read a per-file report — a single edit
should address the whole cluster, not one rule at a time:

- **Extraction + ordering + imports**: `InlineRegexConstant`,
  `DuplicateRegexConstant`, `CollapsibleConstantConcatenation`,
  `CommonsLang3StringConstant` → create new `static final` fields that must
  then satisfy `StaticFinalFirst` and may enable new `StaticImportCandidate`
  suggestions.
- **Line-length churn**: `UnnecessaryLineWrap`, `CompactableParameterList`,
  `ChainedCallLineBreak`, `MethodCallArgumentsOnSameLine` — any edit in a
  long line can flip the verdict of the others; resolve them together.
- **Variable flow**: `SingleUseLocalVariable`, `PureSingleUseLocalVariable`,
  `SplitDeclarationAssignment`, `BooleanFromCondition`, `IfReturnBooleanLiteral`,
  `ConditionalReturnToTernary` — inlining one variable can create or kill the
  preconditions for the others. Plan the final shape before editing.
- **Duplication + extraction**: `DuplicateMethodBodyCheck` (`...extractable`
  variant), `DuplicateRegexConstantCheck`, `TestOnlyDelegateCheck`,
  `RepeatedExceptionWrappingCheck` — these all want you to move code to a
  shared location. Do the moves in one pass across all affected files so the
  shared helper lands with its call sites updated.

---

## Requirements

- **Java 25+**
- **Maven 3.9+**

## License

MIT
