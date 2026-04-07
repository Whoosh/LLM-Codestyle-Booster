package io.github.llmcodestyle.quality;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import io.github.llmcodestyle.utils.AstUtil;

import static com.puppycrawl.tools.checkstyle.api.TokenTypes.ABSTRACT;
import static com.puppycrawl.tools.checkstyle.api.TokenTypes.CLASS_DEF;
import static com.puppycrawl.tools.checkstyle.api.TokenTypes.IDENT;
import static com.puppycrawl.tools.checkstyle.api.TokenTypes.INTERFACE_DEF;
import static com.puppycrawl.tools.checkstyle.api.TokenTypes.LITERAL_DEFAULT;
import static com.puppycrawl.tools.checkstyle.api.TokenTypes.LITERAL_PRIVATE;
import static com.puppycrawl.tools.checkstyle.api.TokenTypes.LITERAL_PROTECTED;
import static com.puppycrawl.tools.checkstyle.api.TokenTypes.METHOD_DEF;
import static com.puppycrawl.tools.checkstyle.api.TokenTypes.PARAMETER_DEF;
import static com.puppycrawl.tools.checkstyle.api.TokenTypes.PARAMETERS;
import static com.puppycrawl.tools.checkstyle.api.TokenTypes.RECORD_COMPONENT_DEF;
import static com.puppycrawl.tools.checkstyle.api.TokenTypes.RECORD_COMPONENTS;
import static com.puppycrawl.tools.checkstyle.api.TokenTypes.RECORD_DEF;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Flags public and package-private methods in production classes that are not referenced in the corresponding test class. */
public class PublicMethodTestCoverageCheck extends AbstractCheck {

    /** Violation message key. */
    static final String MSG_KEY = "public.method.not.tested";

    private static final Set<String> EXEMPT_METHODS = Set.of(
        "toString",
        "hashCode",
        "equals",
        "compareTo",
        "clone",
        "getDefaultTokens",
        "getAcceptableTokens",
        "getRequiredTokens",
        "beginTree",
        "finishTree",
        "visitToken",
        "leaveToken",
        "isCommentNodesRequired",
        "init",
        "destroy",
        "processFiltered"
    );

    private static final String MAIN_SOURCE_MARKER = "/src/main/";
    private static final String TEST_SOURCE_MARKER = "/src/test/";
    private static final String TEST_SUFFIX = "Test.java";
    private static final String SLOW_TEST_SUFFIX = "SlowTest.java";

    private final List<MethodInfo> methods = new ArrayList<>();
    private final Set<String> recordComponents = new TreeSet<>();
    private boolean isTestFile;
    private boolean isInterface;
    private String className = "";

    @Override
    public int[] getDefaultTokens() {
        return getRequiredTokens();
    }

    @Override
    public int[] getAcceptableTokens() {
        return getRequiredTokens();
    }

    @Override
    public int[] getRequiredTokens() {
        return new int[]{CLASS_DEF, INTERFACE_DEF, RECORD_DEF, METHOD_DEF};
    }

    @Override
    public void beginTree(DetailAST rootAST) {
        methods.clear();
        recordComponents.clear();
        isInterface = false;
        className = "";

        String fileName = getFileContents().getFileName();
        isTestFile = fileName.contains(TEST_SOURCE_MARKER) || fileName.endsWith(TEST_SUFFIX) || fileName.endsWith(SLOW_TEST_SUFFIX);
    }

    @Override
    public void visitToken(DetailAST ast) {
        if (isTestFile) {
            return;
        }
        int type = ast.getType();
        if ((type == CLASS_DEF || type == INTERFACE_DEF || type == RECORD_DEF) && AstUtil.typeNestingDepth(ast) == 0) {
            DetailAST ident = ast.findFirstToken(IDENT);
            if (ident != null && className.isEmpty()) {
                className = ident.getText();
            }
        }
        if (type == INTERFACE_DEF && AstUtil.typeNestingDepth(ast) == 0) {
            isInterface = true;
        } else if (type == RECORD_DEF && AstUtil.typeNestingDepth(ast) == 0) {
            collectRecordComponents(ast);
        } else if (type == METHOD_DEF) {
            collectMethodIfEligible(ast);
        }
    }

    @Override
    public void finishTree(DetailAST rootAST) {
        if (isTestFile || methods.isEmpty() || className.isEmpty()) {
            return;
        }
        String testContent = readTestFiles(getFileContents().getFileName());
        if (testContent == null) {
            return;
        }
        if (!testContent.contains(className)) {
            for (MethodInfo method : methods) {
                log(method.line(), 0, MSG_KEY, method.name());
            }
            return;
        }
        Set<String> instanceVars = findInstanceVariables(testContent, className);

        for (MethodInfo method : methods) {
            if (!isMethodCovered(testContent, className, method.name(), instanceVars)) {
                log(method.line(), 0, MSG_KEY, method.name());
            }
        }
    }

    /** Context-aware method coverage check. Returns true if the method appears to be called on an instance of this class. */
    private static boolean isMethodCovered(String testContent, String className, String methodName, Set<String> instanceVars) {
        String call = methodName + "(";
        if (testContent.contains(className + "." + call)) {
            return true;
        }
        if (testContent.contains(className + "::" + methodName)) {
            return true;
        }
        for (String varName : instanceVars) {
            if (testContent.contains(varName + "." + call) || testContent.contains(varName + "::" + methodName)) {
                return true;
            }
        }
        return testContent.contains("new " + className + "(") && testContent.contains("." + call);
    }

    /** Finds variable names declared with the given class type in test content. */
    private static Set<String> findInstanceVariables(String testContent, String className) {
        Set<String> vars = new HashSet<>();
        Matcher m1 = Pattern.compile("\\b" + Pattern.quote(className) + "\\s+(\\w+)").matcher(testContent);
        while (m1.find()) {
            String candidate = m1.group(1);
            if (!"class".equals(candidate) && !"extends".equals(candidate) && !"implements".equals(candidate)) {
                vars.add(candidate);
            }
        }
        Matcher m2 = Pattern.compile("\\bvar\\s+(\\w+)\\s*=\\s*new\\s+" + Pattern.quote(className) + "\\s*\\(").matcher(testContent);
        while (m2.find()) {
            vars.add(m2.group(1));
        }
        return vars;
    }

    private void collectMethodIfEligible(DetailAST methodDef) {
        if (AstUtil.typeNestingDepth(methodDef) > 1) {
            return;
        }
        if (AstUtil.hasModifier(methodDef, ABSTRACT)) {
            return;
        }
        if (isInterface && !AstUtil.hasModifier(methodDef, LITERAL_DEFAULT)) {
            return;
        }
        if (AstUtil.hasModifier(methodDef, LITERAL_PRIVATE) || AstUtil.hasModifier(methodDef, LITERAL_PROTECTED)) {
            return;
        }
        DetailAST ident = methodDef.findFirstToken(IDENT);
        if (ident == null) {
            return;
        }
        String name = ident.getText();
        if (EXEMPT_METHODS.contains(name)) {
            return;
        }
        if ("main".equals(name) && hasStringArrayParam(methodDef)) {
            return;
        }
        if (recordComponents.contains(name)) {
            return;
        }
        methods.add(new MethodInfo(name, ident.getLineNo()));
    }

    private void collectRecordComponents(DetailAST recordDef) {
        DetailAST components = recordDef.findFirstToken(RECORD_COMPONENTS);
        if (components == null) {
            return;
        }
        DetailAST child = components.getFirstChild();
        while (child != null) {
            if (child.getType() == RECORD_COMPONENT_DEF) {
                DetailAST ident = child.findFirstToken(IDENT);
                if (ident != null) {
                    recordComponents.add(ident.getText());
                }
            }
            child = child.getNextSibling();
        }
    }

    private static boolean hasStringArrayParam(DetailAST methodDef) {
        DetailAST params = methodDef.findFirstToken(PARAMETERS);
        if (params == null) {
            return false;
        }
        int paramCount = 0;
        DetailAST child = params.getFirstChild();
        while (child != null) {
            if (child.getType() == PARAMETER_DEF) {
                paramCount++;
            }
            child = child.getNextSibling();
        }
        return paramCount == 1;
    }

    private static String readTestFiles(String mainFilePath) {
        String normalized = mainFilePath.replace('\\', '/');
        int mainIdx = normalized.indexOf(MAIN_SOURCE_MARKER);
        if (mainIdx < 0) {
            return null;
        }
        String relative = normalized.substring(mainIdx + MAIN_SOURCE_MARKER.length());
        String baseName = relative.substring(0, relative.length() - ".java".length());

        Path testDir = Path.of(normalized.substring(0, mainIdx), "src", "test");
        StringBuilder content = new StringBuilder();
        appendFileIfExists(testDir.resolve(baseName + TEST_SUFFIX), content);
        appendFileIfExists(testDir.resolve(baseName + SLOW_TEST_SUFFIX), content);
        return content.isEmpty() ? null : content.toString();
    }

    private static void appendFileIfExists(Path path, StringBuilder out) {
        if (Files.exists(path)) {
            try {
                out.append(Files.readString(path, UTF_8));
            } catch (IOException ignored) {
                // If we can't read the test file, skip silently
            }
        }
    }

    private record MethodInfo(String name, int line) {
    }
}
