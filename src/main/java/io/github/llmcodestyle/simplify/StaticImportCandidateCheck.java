package io.github.llmcodestyle.simplify;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;

import static com.puppycrawl.tools.checkstyle.api.TokenTypes.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Suggests static imports for two reference shapes:
 * <ul>
 *   <li>{@code ClassName.CONSTANT_NAME} — any qualified reference to an
 *       UPPER_SNAKE_CASE field on a class whose name starts with an uppercase letter.</li>
 *   <li>{@code UtilClass.methodName(...)} — any method call whose receiver is a
 *       simple identifier ending in {@code Util} or {@code Utils}. The convention
 *       in this codebase is that utility classes should be star-statically-imported
 *       so that call sites read as bare function calls.</li>
 * </ul>
 */
public class StaticImportCandidateCheck extends AbstractCheck {

    /**
     * Violation message key.
     */
    static final String MSG_KEY = "static.import.candidate";
    private static final int[] TOKENS = {STATIC_IMPORT, DOT};

    private final Map<String, Integer> qualifiedRefs = new HashMap<>();
    private final Set<String> staticImports = new HashSet<>();
    private final Map<String, Integer> firstLines = new HashMap<>();

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
    public void beginTree(DetailAST rootAST) {
        qualifiedRefs.clear();
        staticImports.clear();
        firstLines.clear();
    }

    @Override
    public void visitToken(DetailAST ast) {
        if (ast.getType() == STATIC_IMPORT) {
            recordStaticImport(ast);
        } else if (ast.getType() == DOT) {
            processDotAccess(ast);
        }
    }

    @Override
    public void finishTree(DetailAST rootAST) {
        Map<String, Integer> constantNameToClassCount = new HashMap<>();
        for (String key : qualifiedRefs.keySet()) {
            constantNameToClassCount.merge(key.substring(key.lastIndexOf('.') + 1), 1, Integer::sum);
        }

        for (Map.Entry<String, Integer> entry : qualifiedRefs.entrySet()) {
            String qualifiedRef = entry.getKey();
            String constantName = qualifiedRef.substring(qualifiedRef.lastIndexOf('.') + 1);
            if (staticImports.contains(constantName) || constantNameToClassCount.getOrDefault(constantName, 0) > 1 && !qualifiedRef.equals(findWinner(constantName))) {
                continue;
            }
            Integer lineNo = firstLines.get(qualifiedRef);
            if (lineNo != null) {
                log(lineNo, 0, MSG_KEY, qualifiedRef.substring(0, qualifiedRef.lastIndexOf('.')), constantName, entry.getValue());
            }
        }
    }

    private Map<String, Integer> collectCandidates(String constantName) {
        Map<String, Integer> result = new HashMap<>();
        for (Map.Entry<String, Integer> entry : qualifiedRefs.entrySet()) {
            String key = entry.getKey();
            if (constantName.equals(key.substring(key.lastIndexOf('.') + 1))) {
                result.put(key, entry.getValue());
            }
        }
        return result;
    }

    private String findWinner(String constantName) {
        return collectCandidates(constantName).entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(null);
    }

    private void recordStaticImport(DetailAST staticImportAst) {
        DetailAST lastIdent = findLastIdent(staticImportAst);
        if (lastIdent != null) {
            staticImports.add(lastIdent.getText());
        }
    }

    private static DetailAST findLastIdent(DetailAST ast) {
        DetailAST last = null;
        DetailAST child = ast.getFirstChild();
        while (child != null) {
            if (child.getType() == IDENT) {
                last = child;
            } else if (child.getType() == DOT || child.getType() == STAR) {
                DetailAST sub = findLastIdent(child);
                if (sub != null) {
                    last = sub;
                }
            }
            child = child.getNextSibling();
        }
        return last;
    }

    private void processDotAccess(DetailAST dot) {
        if (isInsideImport(dot)) {
            return;
        }
        DetailAST left = dot.getFirstChild();
        DetailAST right = dot.getLastChild();
        if (left == null || right == null || left.getType() != IDENT || right.getType() != IDENT) {
            return;
        }
        String className = left.getText();
        String memberName = right.getText();
        if (className.isEmpty() || !Character.isUpperCase(className.charAt(0)) || !(isUpperCaseConstant(memberName) || isUtilClassName(className) && isInsideMethodCall(dot))) {
            return;
        }
        String key = className + "." + memberName;
        qualifiedRefs.merge(key, 1, Integer::sum);
        firstLines.putIfAbsent(key, dot.getLineNo());
    }

    private static boolean isUtilClassName(String name) {
        return name.endsWith("Util") || name.endsWith("Utils");
    }

    private static boolean isInsideMethodCall(DetailAST dot) {
        DetailAST parent = dot.getParent();
        return parent != null && parent.getType() == METHOD_CALL;
    }

    private static boolean isUpperCaseConstant(String name) {
        if (name.isEmpty()) {
            return false;
        }
        for (char c : name.toCharArray()) {
            if (c != '_' && !Character.isUpperCase(c) && !Character.isDigit(c)) {
                return false;
            }
        }
        for (char c : name.toCharArray()) {
            if (Character.isUpperCase(c)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isInsideImport(DetailAST ast) {
        DetailAST parent = ast.getParent();
        while (parent != null) {
            if (parent.getType() == IMPORT || parent.getType() == STATIC_IMPORT) {
                return true;
            }
            parent = parent.getParent();
        }
        return false;
    }
}
