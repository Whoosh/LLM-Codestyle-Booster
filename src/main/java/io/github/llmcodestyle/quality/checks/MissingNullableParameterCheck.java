package io.github.llmcodestyle.quality.checks;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import io.github.llmcodestyle.pojos.MethodParamInfo;
import jakarta.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.puppycrawl.tools.checkstyle.api.TokenTypes.*;
import static io.github.llmcodestyle.utils.AstAnnotationUtil.*;
import static io.github.llmcodestyle.utils.AstMethodCallUtil.*;
import static io.github.llmcodestyle.utils.AstUtil.*;

/**
 * Flags method/constructor parameters that are null-checked in the body without
 * {@code @Nullable}, and call sites that pass {@code null} to methods not handling it.
 *
 * <p>Two detection modes:
 * <ul>
 *   <li><b>Missing annotation</b>: a parameter is compared to {@code null} in the body
 *       (non-rejection pattern) but lacks {@code @Nullable}.</li>
 *   <li><b>Unhandled null call</b>: a {@code null} literal is passed to a same-class
 *       method whose corresponding parameter has neither {@code @Nullable} nor a null
 *       check in the body.</li>
 * </ul>
 *
 * <p>Null-rejection patterns ({@code if (param == null) throw ...}) are excluded.
 * All methods and constructors inside {@code record} types are excluded.
 */
public class MissingNullableParameterCheck extends AbstractCheck {

    static final String MSG_KEY = "missing.nullable.parameter";
    static final String MSG_KEY_CALL = "missing.nullable.parameter.call";

    private static final int[] TOKENS = {METHOD_DEF, CTOR_DEF, METHOD_CALL};
    private static final String NULLABLE = "Nullable";
    private static final Set<Integer> METHOD_DEF_TOKENS = Set.of(METHOD_DEF, CTOR_DEF);
    private static final Set<Integer> NULL_COMP_TOKENS = Set.of(EQUAL, NOT_EQUAL);
    private static final Set<Integer> SCOPE_BOUNDARY_TOKENS = Set.of(CLASS_DEF, RECORD_DEF, ENUM_DEF, INTERFACE_DEF, LAMBDA);

    private final Map<DetailAST, Map<String, List<MethodParamInfo>>> classMethodMap = new IdentityHashMap<>();

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
        classMethodMap.clear();
        collectClassMethodInfo(rootAST);
    }

    @Override
    public void visitToken(DetailAST ast) {
        if (METHOD_DEF_TOKENS.contains(ast.getType())) {
            checkMissingNullable(ast);
        } else if (ast.getType() == METHOD_CALL) {
            checkNullLiteralArg(ast);
        }
    }

    // ---- Case 1: null-checked param without @Nullable ----
    private void checkMissingNullable(DetailAST def) {
        if (isInsideRecord(def)) {
            return;
        }
        DetailAST params = def.findFirstToken(PARAMETERS);
        DetailAST body = def.findFirstToken(SLIST);
        if (params == null || body == null) {
            return;
        }
        String name = methodName(def);
        for (DetailAST p = params.getFirstChild(); p != null; p = p.getNextSibling()) {
            if (p.getType() != PARAMETER_DEF || hasAnyAnnotationNamed(p, NULLABLE)) {
                continue;
            }
            DetailAST ident = p.findFirstToken(IDENT);
            if (ident != null && hasNonRejectionNullCheck(ident.getText(), body)) {
                log(p, MSG_KEY, ident.getText(), name);
            }
        }
    }

    // ---- Case 2: null literal passed to method without null handling ----
    private void checkNullLiteralArg(DetailAST call) {
        String called = extractLocalMethodName(call);
        if (called == null) {
            return;
        }
        DetailAST elist = call.findFirstToken(ELIST);
        if (elist == null) {
            return;
        }
        List<Integer> nullIdx = nullLiteralArgIndices(elist);
        if (nullIdx.isEmpty()) {
            return;
        }
        Map<String, List<MethodParamInfo>> methods = classMethodMap.get(findEnclosingTypeDef(call));
        if (methods == null) {
            return;
        }
        int argCount = countExprs(elist);
        List<MethodParamInfo> matches = methods.getOrDefault(called, List.of()).stream()
            .filter(m -> m.paramCount() == argCount)
            .toList();
        if (matches.size() != 1) {
            return;
        }
        MethodParamInfo info = matches.get(0);
        for (int i : nullIdx) {
            if (i < info.paramCount() && !info.nullableIndices().contains(i) && !info.nullAcceptedIndices().contains(i)) {
                log(call, MSG_KEY_CALL, info.paramNames().get(i), called);
            }
        }
    }

    // ---- Pre-collection phase ----
    private void collectClassMethodInfo(@Nullable DetailAST node) {
        if (node == null) {
            return;
        }
        if (CLASS_LIKE_TYPES.contains(node.getType()) && !isInsideRecord(node)) {
            classMethodMap.put(node, buildMethodMap(node));
        }
        for (DetailAST c = node.getFirstChild(); c != null; c = c.getNextSibling()) {
            collectClassMethodInfo(c);
        }
    }

    private static Map<String, List<MethodParamInfo>> buildMethodMap(DetailAST typeDef) {
        DetailAST objBlock = typeDef.findFirstToken(OBJBLOCK);
        if (objBlock == null) {
            return Map.of();
        }
        Map<String, List<MethodParamInfo>> map = new HashMap<>();
        for (DetailAST child = objBlock.getFirstChild(); child != null;
             child = child.getNextSibling()) {
            addMethodDef(child, map);
        }
        return map;
    }

    private static void addMethodDef(DetailAST child, Map<String, List<MethodParamInfo>> map) {
        if (!METHOD_DEF_TOKENS.contains(child.getType())) {
            return;
        }
        DetailAST params = child.findFirstToken(PARAMETERS);
        DetailAST body = child.findFirstToken(SLIST);
        if (params == null || body == null) {
            return;
        }
        map.computeIfAbsent(methodName(child), k -> new ArrayList<>()).add(analyzeParams(params, body));
    }

    private static MethodParamInfo analyzeParams(DetailAST params, DetailAST body) {
        List<String> names = new ArrayList<>();
        Set<Integer> nullable = new HashSet<>();
        Set<Integer> accepted = new HashSet<>();
        int idx = 0;
        for (DetailAST p = params.getFirstChild(); p != null; p = p.getNextSibling()) {
            if (p.getType() != PARAMETER_DEF) {
                continue;
            }
            DetailAST ident = p.findFirstToken(IDENT);
            String pName = ident != null ? ident.getText() : "?";
            names.add(pName);
            if (hasAnyAnnotationNamed(p, NULLABLE)) {
                nullable.add(idx);
            }
            if (ident != null && hasNonRejectionNullCheck(pName, body)) {
                accepted.add(idx);
            }
            idx++;
        }
        return new MethodParamInfo(idx, names, nullable, accepted);
    }

    // ---- Null-comparison analysis ----
    private static boolean hasNonRejectionNullCheck(String paramName, DetailAST body) {
        List<DetailAST> comps = new ArrayList<>();
        collectNullComparisons(paramName, body, comps);
        return comps.stream().anyMatch(c -> !isNullRejection(c));
    }

    private static void collectNullComparisons(String paramName, @Nullable DetailAST node, List<DetailAST> out) {
        if (node == null) {
            return;
        }
        int type = node.getType();
        if (SCOPE_BOUNDARY_TOKENS.contains(type)) {
            return;
        }
        if (NULL_COMP_TOKENS.contains(type) && matchesNullComparison(paramName, node)) {
            out.add(node);
        }
        for (DetailAST c = node.getFirstChild(); c != null; c = c.getNextSibling()) {
            collectNullComparisons(paramName, c, out);
        }
    }

    private static boolean matchesNullComparison(String name, DetailAST comp) {
        DetailAST left = comp.getFirstChild();
        DetailAST right = left != null ? left.getNextSibling() : null;
        if (left == null || right == null) {
            return false;
        }
        return isIdent(left, name) && right.getType() == LITERAL_NULL || left.getType() == LITERAL_NULL && isIdent(right, name);
    }

    private static boolean isNullRejection(DetailAST comparison) {
        // Walk up through any chain of LAND parents (LOR interrupts the rejection).
        DetailAST top = comparison;
        for (DetailAST parent = top.getParent(); parent != null && parent.getType() == LAND; parent = parent.getParent()) {
            top = parent;
        }
        DetailAST expr = top.getParent();
        if (expr == null || expr.getType() != EXPR) {
            return false;
        }
        DetailAST ifNode = expr.getParent();
        if (ifNode == null || ifNode.getType() != LITERAL_IF) {
            return false;
        }
        if (comparison.getType() == EQUAL) {
            return containsThrow(ifNode.findFirstToken(SLIST));
        }
        DetailAST elseNode = ifNode.findFirstToken(LITERAL_ELSE);
        return elseNode != null && containsThrow(elseNode.findFirstToken(SLIST));
    }

    private static boolean containsThrow(@Nullable DetailAST node) {
        if (node == null) {
            return false;
        }
        if (node.getType() == LITERAL_THROW) {
            return true;
        }
        if (SCOPE_BOUNDARY_TOKENS.contains(node.getType())) {
            return false;
        }
        for (DetailAST c = node.getFirstChild(); c != null; c = c.getNextSibling()) {
            if (containsThrow(c)) {
                return true;
            }
        }
        return false;
    }

    // ---- Utility ----
    private static boolean isIdent(DetailAST node, String name) {
        return node.getType() == IDENT && name.equals(node.getText());
    }

    private static String methodName(DetailAST def) {
        DetailAST ident = def.findFirstToken(IDENT);
        return ident != null ? ident.getText() : "<init>";
    }

    private static List<Integer> nullLiteralArgIndices(DetailAST elist) {
        List<Integer> indices = new ArrayList<>();
        int idx = 0;
        for (DetailAST c = elist.getFirstChild(); c != null; c = c.getNextSibling()) {
            if (c.getType() == EXPR) {
                if (isNullLiteralExpr(c)) {
                    indices.add(idx);
                }
                idx++;
            }
        }
        return indices;
    }

    private static boolean isNullLiteralExpr(DetailAST expr) {
        DetailAST child = expr.getFirstChild();
        if (child == null) {
            return false;
        }
        return child.getType() == LITERAL_NULL || child.getType() == TYPECAST && child.findFirstToken(LITERAL_NULL) != null;
    }

    private static int countExprs(DetailAST elist) {
        int count = 0;
        for (DetailAST c = elist.getFirstChild(); c != null; c = c.getNextSibling()) {
            if (c.getType() == EXPR) {
                count++;
            }
        }
        return count;
    }

    @Nullable
    private static DetailAST findEnclosingTypeDef(DetailAST node) {
        for (DetailAST p = node.getParent(); p != null; p = p.getParent()) {
            if (CLASS_LIKE_TYPES.contains(p.getType())) {
                return p;
            }
        }
        return null;
    }

    private static boolean isInsideRecord(DetailAST node) {
        for (DetailAST p = node.getParent(); p != null; p = p.getParent()) {
            if (p.getType() == RECORD_DEF) {
                return true;
            }
        }
        return false;
    }

}
