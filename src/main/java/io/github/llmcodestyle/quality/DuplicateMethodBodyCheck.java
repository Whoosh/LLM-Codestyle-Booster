package io.github.llmcodestyle.quality;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.utils.TokenUtil;
import io.github.llmcodestyle.pojos.DuplicateMethodOccurrence;
import io.github.llmcodestyle.utils.AstSingleUseUtil;

import static com.puppycrawl.tools.checkstyle.api.TokenTypes.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Detects methods with structurally identical bodies after normalizing local variable
 * and parameter names. Catches duplicates that CPD and PMD miss when only the identifier
 * names differ — e.g. {@code checkTypeNode(DetailAST typeNode)} and
 * {@code checkBorNode(DetailAST bor)}.
 *
 * <p>Detection accumulates across all files in a single Checkstyle run (TreeWalker reuses
 * the check instance), so cross-class duplicates surface during {@code mvn verify}.
 *
 * <p>The check is deliberately conservative:
 * <ul>
 *   <li>abstract methods, constructors, compact constructors — skipped (no body / contextual wiring)</li>
 *   <li>{@code @Override}-annotated methods — skipped (interface/contract-driven)</li>
 *   <li>bodies with fewer than {@code minStatements} top-level statements — skipped</li>
 *   <li>bodies with more than {@code maxBodyNodes} AST nodes — skipped (asymptotic safety)</li>
 * </ul>
 *
 * <p>Normalization:
 * <ul>
 *   <li>All parameter and local variable names, in declaration order, are mapped to positional
 *       placeholders ({@code $n0}, {@code $n1}, ...).</li>
 *   <li>Literal values (strings, numbers, booleans) are preserved verbatim — differing literals
 *       yield different hashes.</li>
 *   <li>Method names, type names, and field references are preserved verbatim — differing calls
 *       yield different hashes.</li>
 *   <li>Operators and structural tokens are emitted by token type name.</li>
 * </ul>
 */
public class DuplicateMethodBodyCheck extends AbstractCheck {

    /**
     * Violation message key.
     */
    static final String MSG_KEY = "duplicate.method.body";
    private static final int[] TOKENS = {METHOD_DEF};

    private static final int DEFAULT_MIN_STATEMENTS = 2;
    private static final int DEFAULT_MAX_BODY_NODES = 400;

    private static final Set<Integer> LITERAL_TOKENS = Set.of(
        STRING_LITERAL,
        NUM_INT,
        NUM_LONG,
        NUM_FLOAT,
        NUM_DOUBLE,
        CHAR_LITERAL,
        LITERAL_TRUE,
        LITERAL_FALSE,
        LITERAL_NULL,
        TEXT_BLOCK_CONTENT);

    private static final Set<Integer> NESTED_TYPE_TOKENS = Set.of(CLASS_DEF, INTERFACE_DEF, ENUM_DEF, RECORD_DEF, ANNOTATION_DEF);

    private int minStatements = DEFAULT_MIN_STATEMENTS;
    private int maxBodyNodes = DEFAULT_MAX_BODY_NODES;

    private final Map<String, DuplicateMethodOccurrence> seenBodies = new HashMap<>();

    /**
     * Set the minimum number of top-level statements in the body for a method to be considered.
     */
    public void setMinStatements(int minStatements) {
        this.minStatements = minStatements;
    }

    /**
     * Set the maximum number of AST nodes in a body above which the method is ignored
     * (asymptotic safety cap).
     */
    public void setMaxBodyNodes(int maxBodyNodes) {
        this.maxBodyNodes = maxBodyNodes;
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
    public void visitToken(DetailAST methodDef) {
        if (isOverride(methodDef)) {
            return;
        }
        DetailAST slist = methodDef.findFirstToken(SLIST);
        if (slist == null || AstSingleUseUtil.collectStatements(slist).size() < minStatements || countNodes(slist) > maxBodyNodes) {
            return;
        }

        String normalized = normalize(methodDef, slist);
        String methodName = extractName(methodDef);
        String className = extractEnclosingClassName(methodDef);

        DuplicateMethodOccurrence previous = seenBodies.get(normalized);
        if (previous != null) {
            log(methodDef, MSG_KEY, methodName, previous.methodName(), previous.className());
        } else {
            seenBodies.put(normalized, new DuplicateMethodOccurrence(className, methodName));
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        seenBodies.clear();
    }

    private static boolean isOverride(DetailAST methodDef) {
        DetailAST modifiers = methodDef.findFirstToken(MODIFIERS);
        if (modifiers == null) {
            return false;
        }
        for (DetailAST mod = modifiers.getFirstChild(); mod != null; mod = mod.getNextSibling()) {
            if (mod.getType() == ANNOTATION) {
                DetailAST ident = mod.findFirstToken(IDENT);
                if (ident != null && "Override".equals(ident.getText())) {
                    return true;
                }
            }
        }
        return false;
    }

    private static String extractName(DetailAST methodDef) {
        DetailAST ident = methodDef.findFirstToken(IDENT);
        return ident != null ? ident.getText() : "<anon>";
    }

    private static String extractEnclosingClassName(DetailAST methodDef) {
        DetailAST parent = methodDef.getParent();
        while (parent != null) {
            if (NESTED_TYPE_TOKENS.contains(parent.getType())) {
                DetailAST ident = parent.findFirstToken(IDENT);
                if (ident != null) {
                    return ident.getText();
                }
            }
            parent = parent.getParent();
        }
        return "<unknown>";
    }

    private static int countNodes(DetailAST node) {
        int count = 1;
        for (DetailAST child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
            count += countNodes(child);
        }
        return count;
    }

    private static String normalize(DetailAST methodDef, DetailAST slist) {
        Map<String, String> nameMap = new LinkedHashMap<>();
        collectParamNames(methodDef, nameMap);
        collectLocalNames(slist, nameMap, new HashSet<>());

        StringBuilder sb = new StringBuilder();
        serialize(slist, sb, nameMap);
        return sb.toString();
    }

    private static void collectParamNames(DetailAST methodDef, Map<String, String> nameMap) {
        DetailAST parameters = methodDef.findFirstToken(PARAMETERS);
        if (parameters == null) {
            return;
        }
        for (DetailAST child = parameters.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child.getType() == PARAMETER_DEF) {
                DetailAST ident = child.findFirstToken(IDENT);
                if (ident != null) {
                    assignIfAbsent(nameMap, ident.getText());
                }
            }
        }
    }

    private static void collectLocalNames(DetailAST node, Map<String, String> nameMap, Set<DetailAST> visited) {
        if (!visited.add(node)) {
            return;
        }
        int type = node.getType();
        if (type == VARIABLE_DEF || type == PARAMETER_DEF || type == RESOURCE) {
            DetailAST ident = node.findFirstToken(IDENT);
            if (ident != null) {
                assignIfAbsent(nameMap, ident.getText());
            }
        }
        for (DetailAST child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
            collectLocalNames(child, nameMap, visited);
        }
    }

    private static void assignIfAbsent(Map<String, String> nameMap, String name) {
        nameMap.computeIfAbsent(name, k -> "$n" + nameMap.size());
    }

    private static void serialize(DetailAST node, StringBuilder sb, Map<String, String> nameMap) {
        int type = node.getType();
        if (type == IDENT) {
            String replacement = nameMap.get(node.getText());
            sb.append(replacement != null ? replacement : node.getText());
        } else if (LITERAL_TOKENS.contains(type)) {
            sb.append(node.getText());
        } else {
            sb.append(TokenUtil.getTokenName(type));
        }
        sb.append('|');
        for (DetailAST child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
            serialize(child, sb, nameMap);
        }
        sb.append('/');
    }
}
