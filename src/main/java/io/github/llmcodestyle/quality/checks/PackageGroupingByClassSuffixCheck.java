package io.github.llmcodestyle.quality.checks;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import io.github.llmcodestyle.pojos.TopLevelTypeDecl;
import jakarta.annotation.Nullable;

import static com.puppycrawl.tools.checkstyle.api.TokenTypes.*;
import static io.github.llmcodestyle.utils.AstUtil.*;
import static java.nio.charset.StandardCharsets.*;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Locale.*;

/**
 * Flags top-level types whose suffix-group reaches a configurable size in a package that still
 * mixes in other non-POJO classes. Such groups should be moved into a dedicated subpackage named
 * after the (pluralized) suffix. POJO carriers (records, enums, interfaces, {@code *Dto},
 * {@code *DTO}, {@code *Pojo}) do not count as "other" classes and are themselves never flagged.
 */
public class PackageGroupingByClassSuffixCheck extends AbstractCheck {

    /**
     * Violation message key.
     */
    static final String MSG_KEY = "package.grouping.class.suffix";
    private static final int[] TOKENS = {CLASS_DEF, RECORD_DEF, ENUM_DEF, INTERFACE_DEF};
    private static final int DEFAULT_MIN_GROUP_SIZE = 5;

    private static final Set<String> POJO_NAME_SUFFIXES = Set.of("Dto", "DTO", "Pojo");
    private static final Set<Integer> POJO_AST_TOKENS = Set.of(RECORD_DEF, ENUM_DEF, INTERFACE_DEF);
    private static final Set<Character> VOWELS = Set.of('a', 'e', 'i', 'o', 'u');
    private static final String STRIPPED_STRING_LITERAL = "\"\"";
    private static final Pattern TYPE_DECL_PATTERN = Pattern.compile("\\b(class|record|enum|interface)\\s+(\\w+)");
    private static final Pattern BLOCK_COMMENT = Pattern.compile("(?s)/\\*.*?\\*/");
    private static final Pattern LINE_COMMENT = Pattern.compile("(?m)//[^\n]*");
    private static final Pattern STRING_LITERAL = Pattern.compile("\"(?:[^\"\\\\]|\\\\.)*\"");
    private static final Pattern CAMEL_BOUNDARY = Pattern.compile("(?<=[a-z])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z])");

    private final Map<Path, Map<String, Integer>> dirGroupCache = new HashMap<>();
    private int minGroupSize = DEFAULT_MIN_GROUP_SIZE;

    /**
     * Set the minimum size of a same-suffix group that triggers the rule.
     */
    public void setMinGroupSize(int minGroupSize) {
        this.minGroupSize = minGroupSize;
    }

    @Override
    public int[] getDefaultTokens() {
        return TOKENS.clone();
    }

    @Override
    public int[] getAcceptableTokens() {
        return TOKENS.clone();
    }

    @Override
    public int[] getRequiredTokens() {
        return TOKENS.clone();
    }

    @Override
    public void visitToken(DetailAST ast) {
        if (typeNestingDepth(ast) > 0) {
            return;
        }
        DetailAST ident = ast.findFirstToken(IDENT);
        if (ident == null) {
            return;
        }
        String myName = ident.getText();
        if (isPojoKind(ast.getType()) || hasPojoNameSuffix(myName)) {
            return;
        }
        Path dir = parentDirOfCurrentFile();
        if (dir == null) {
            return;
        }
        Map<String, Integer> groupCounts = dirGroupCache.computeIfAbsent(dir, PackageGroupingByClassSuffixCheck::analyzeDir);
        String suffix = lastCamelWord(myName);
        if (suffix == null) {
            return;
        }
        Integer myGroupSize = groupCounts.get(suffix);
        if (myGroupSize == null || myGroupSize < minGroupSize || sumOf(groupCounts) <= myGroupSize) {
            return;
        }
        log(ident.getLineNo(), ident.getColumnNo(), MSG_KEY, myName, suffix, myGroupSize, pluralize(suffix));
    }

    @Nullable
    private Path parentDirOfCurrentFile() {
        String filePath = getFilePath();
        if (filePath == null || filePath.isEmpty()) {
            return null;
        }
        return Path.of(filePath).getParent();
    }

    private static Map<String, Integer> analyzeDir(Path dir) {
        Map<String, Integer> groupCounts = new HashMap<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.java")) {
            for (Path file : stream) {
                if (file.endsWith("package-info.java")) {
                    continue;
                }
                TopLevelTypeDecl decl = parseTopLevelType(file);
                if (decl == null || isPojoKind(decl.kind()) || hasPojoNameSuffix(decl.name())) {
                    continue;
                }
                String suffix = lastCamelWord(decl.name());
                if (suffix != null) {
                    groupCounts.merge(suffix, 1, Integer::sum);
                }
            }
        } catch (IOException ignored) {
            return Map.of();
        }
        return Map.copyOf(groupCounts);
    }

    private static int sumOf(Map<String, Integer> counts) {
        int total = 0;
        for (Integer value : counts.values()) {
            total += value;
        }
        return total;
    }

    @Nullable
    private static TopLevelTypeDecl parseTopLevelType(Path file) {
        String text;
        try {
            text = Files.readString(file, UTF_8);
        } catch (IOException ignored) {
            return null;
        }
        Matcher matcher = TYPE_DECL_PATTERN.matcher(stripNonCode(text));
        if (matcher.find()) {
            return new TopLevelTypeDecl(matcher.group(1), matcher.group(2));
        }
        return null;
    }

    private static String stripNonCode(String text) {
        return STRING_LITERAL.matcher(LINE_COMMENT.matcher(BLOCK_COMMENT.matcher(text).replaceAll("")).replaceAll("")).replaceAll(STRIPPED_STRING_LITERAL);
    }

    private static boolean isPojoKind(String kind) {
        return "record".equals(kind) || "enum".equals(kind) || "interface".equals(kind);
    }

    private static boolean isPojoKind(int astType) {
        return POJO_AST_TOKENS.contains(astType);
    }

    private static boolean hasPojoNameSuffix(String name) {
        for (String suffix : POJO_NAME_SUFFIXES) {
            if (name.endsWith(suffix)) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    static String lastCamelWord(String name) {
        if (name.isEmpty()) {
            return null;
        }
        String[] parts = CAMEL_BOUNDARY.split(name);
        return parts[parts.length - 1];
    }

    static String pluralize(String word) {
        String lower = word.toLowerCase(ROOT);
        int len = lower.length();
        if (len > 1 && lower.charAt(len - 1) == 'y' && !VOWELS.contains(lower.charAt(len - 2))) {
            return lower.substring(0, len - 1) + "ies";
        }
        if (lower.endsWith("ch") || lower.endsWith("sh") || endsWithAnyOf(lower, 's', 'x', 'z')) {
            return lower + "es";
        }
        return lower + "s";
    }

    private static boolean endsWithAnyOf(String s, char a, char b, char c) {
        char last = s.charAt(s.length() - 1);
        return last == a || last == b || last == c;
    }
}
