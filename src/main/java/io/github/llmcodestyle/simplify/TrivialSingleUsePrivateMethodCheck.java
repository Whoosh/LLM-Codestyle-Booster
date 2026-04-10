package io.github.llmcodestyle.simplify;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import io.github.llmcodestyle.utils.AstAnnotationUtil;
import io.github.llmcodestyle.utils.AstSingleUseUtil;
import io.github.llmcodestyle.utils.AstUtil;

import static com.puppycrawl.tools.checkstyle.api.TokenTypes.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Detects {@code private} methods whose body is a single trivial statement and that are
 * called from exactly one site within the enclosing type. Such methods add an indirection
 * without justification — inlining the body removes a name and a stack frame without
 * duplicating any work.
 *
 * <p>To stay safe the check requires:
 * <ul>
 *   <li>method is {@code private} (no polymorphism risk)</li>
 *   <li>method name is unique within the enclosing type (no overloads we cannot
 *       distinguish at the AST level)</li>
 *   <li>body is exactly one statement: {@code return EXPR;}, {@code EXPR;} (an
 *       expression statement) or {@code throw EXPR;}</li>
 *   <li>body contains at most {@link #MAX_METHOD_CALLS} method calls — longer
 *       expression chains (e.g. stream pipelines) deserve a descriptive name</li>
 *   <li>each parameter is referenced at most once in the body, so inlining cannot
 *       duplicate side effects of an argument expression</li>
 *   <li>method has no type parameters (would need explicit type witnesses at the
 *       call site)</li>
 *   <li>method is not annotated (the annotation would be lost on inline)</li>
 *   <li>exactly one external call site exists in the type body, ignoring recursive
 *       self-references</li>
 * </ul>
 *
 * <p>Asymptotic safety: a single call site means the body executes the same number of
 * times before and after inlining, even if that call site is itself inside a loop.
 */
public class TrivialSingleUsePrivateMethodCheck extends AbstractCheck {

    /**
     * Violation message key.
     */
    static final String MSG_KEY = "trivial.single.use.private.method";
    private static final int MAX_METHOD_CALLS = 3;
    private static final int[] TOKENS = {CLASS_DEF, RECORD_DEF, ENUM_DEF, INTERFACE_DEF};

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
    public void visitToken(DetailAST typeDef) {
        DetailAST objBlock = typeDef.findFirstToken(OBJBLOCK);
        if (objBlock == null) {
            return;
        }
        Map<String, Integer> nameOccurrences = new HashMap<>();
        Map<String, DetailAST> uniqueCandidates = new HashMap<>();
        for (DetailAST child = objBlock.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child.getType() != METHOD_DEF) {
                continue;
            }
            String name = methodName(child);
            if (name.isEmpty()) {
                continue;
            }
            nameOccurrences.merge(name, 1, Integer::sum);
            if (isCandidate(child)) {
                uniqueCandidates.put(name, child);
            }
        }
        for (Map.Entry<String, DetailAST> entry : uniqueCandidates.entrySet()) {
            String name = entry.getKey();
            if (nameOccurrences.getOrDefault(name, 0) != 1) {
                continue;
            }
            DetailAST methodDef = entry.getValue();
            if (AstSingleUseUtil.countIdent(objBlock, name) - AstSingleUseUtil.countIdent(methodDef, name) == 1) {
                DetailAST ident = methodDef.findFirstToken(IDENT);
                log(methodDef.getLineNo(), methodDef.getColumnNo(), MSG_KEY, ident != null ? ident.getText() : "?");
            }
        }
    }

    private static boolean isCandidate(DetailAST methodDef) {
        if (!isBareUnannotatedPrivate(methodDef)) {
            return false;
        }
        DetailAST slist = methodDef.findFirstToken(SLIST);
        if (slist == null || !isTrivialBody(slist)) {
            return false;
        }
        return countMethodCalls(slist) <= MAX_METHOD_CALLS && parametersUsedAtMostOnce(methodDef, slist);
    }

    private static boolean isBareUnannotatedPrivate(DetailAST methodDef) {
        return AstUtil.hasModifier(methodDef, LITERAL_PRIVATE)
            && !AstUtil.hasModifier(methodDef, ABSTRACT)
            && !AstUtil.hasModifier(methodDef, LITERAL_NATIVE)
            && !AstAnnotationUtil.hasAnnotationNamed(methodDef, "Override")
            && !hasAnyAnnotation(methodDef)
            && methodDef.findFirstToken(TYPE_PARAMETERS) == null;
    }

    private static int countMethodCalls(DetailAST root) {
        int count = root.getType() == METHOD_CALL ? 1 : 0;
        for (DetailAST child = root.getFirstChild(); child != null; child = child.getNextSibling()) {
            count += countMethodCalls(child);
        }
        return count;
    }

    private static boolean hasAnyAnnotation(DetailAST methodDef) {
        DetailAST modifiers = methodDef.findFirstToken(MODIFIERS);
        if (modifiers == null) {
            return false;
        }
        for (DetailAST mod = modifiers.getFirstChild(); mod != null; mod = mod.getNextSibling()) {
            if (mod.getType() == ANNOTATION) {
                return true;
            }
        }
        return false;
    }

    private static boolean isTrivialBody(DetailAST slist) {
        DetailAST first = null;
        int statementCount = 0;
        for (DetailAST child = slist.getFirstChild(); child != null; child = child.getNextSibling()) {
            int type = child.getType();
            if (type == RCURLY || type == LCURLY || type == SEMI) {
                continue;
            }
            statementCount++;
            if (first == null) {
                first = child;
            }
        }
        if (statementCount != 1 || first == null) {
            return false;
        }
        int type = first.getType();
        return type == LITERAL_RETURN || type == EXPR || type == LITERAL_THROW;
    }

    private static boolean parametersUsedAtMostOnce(DetailAST methodDef, DetailAST slist) {
        DetailAST params = methodDef.findFirstToken(PARAMETERS);
        if (params == null) {
            return true;
        }
        for (DetailAST param = params.getFirstChild(); param != null; param = param.getNextSibling()) {
            if (param.getType() != PARAMETER_DEF) {
                continue;
            }
            DetailAST ident = param.findFirstToken(IDENT);
            if (ident == null) {
                continue;
            }
            if (AstSingleUseUtil.countIdent(slist, ident.getText()) > 1) {
                return false;
            }
        }
        return true;
    }

    private static String methodName(DetailAST methodDef) {
        DetailAST ident = methodDef.findFirstToken(IDENT);
        return ident != null ? ident.getText() : "";
    }
}
