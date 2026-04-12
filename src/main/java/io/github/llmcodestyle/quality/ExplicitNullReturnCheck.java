package io.github.llmcodestyle.quality;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;

import java.util.HashSet;
import java.util.Set;

import static com.puppycrawl.tools.checkstyle.api.TokenTypes.*;
import static io.github.llmcodestyle.utils.AstAnnotationUtil.hasAnyAnnotationNamed;

/**
 * Flags methods that return {@code null} explicitly or delegate to a {@code @Nullable}-annotated
 * method without themselves being annotated {@code @Nullable}.
 *
 * <p>Recognises any annotation whose simple name is {@code Nullable} regardless of package
 * ({@code jakarta.annotation}, {@code javax.annotation}, {@code org.jetbrains.annotations}, etc.).
 *
 * <p>Detection scope:
 * <ul>
 *   <li>{@code return null;} &mdash; direct null literal return.</li>
 *   <li>{@code return foo();} / {@code return this.foo();} where {@code foo()} is declared
 *       {@code @Nullable} in the same compilation unit &mdash; nullable delegation.</li>
 * </ul>
 *
 * <p>Lambdas and nested type boundaries are respected: a {@code return null;} inside a lambda
 * or anonymous/nested class does not flag the enclosing method.
 */
public class ExplicitNullReturnCheck extends AbstractCheck {

    static final String MSG_KEY = "explicit.null.return";
    static final String MSG_KEY_DELEGATION = "explicit.null.return.delegation";

    private static final int[] TOKENS = {LITERAL_RETURN};
    private static final String NULLABLE = "Nullable";

    private final Set<String> nullableMethodNames = new HashSet<>();

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
        nullableMethodNames.clear();
        collectNullableMethods(rootAST);
    }

    @Override
    public void visitToken(DetailAST returnAst) {
        DetailAST expr = returnAst.findFirstToken(EXPR);
        if (expr == null) {
            return;
        }
        DetailAST enclosingMethod = findEnclosingMethod(returnAst);
        if (enclosingMethod == null || hasAnyAnnotationNamed(enclosingMethod, NULLABLE)) {
            return;
        }
        String methodName = enclosingMethod.findFirstToken(IDENT).getText();
        DetailAST child = expr.getFirstChild();

        if (child.getType() == LITERAL_NULL) {
            log(returnAst, MSG_KEY, methodName);
        } else if (child.getType() == METHOD_CALL) {
            checkNullableDelegation(returnAst, child, methodName);
        }
    }

    private void checkNullableDelegation(DetailAST returnAst, DetailAST methodCall, String methodName) {
        String calledName = extractMethodName(methodCall);
        if (calledName != null && nullableMethodNames.contains(calledName)) {
            log(returnAst, MSG_KEY_DELEGATION, methodName, calledName);
        }
    }

    private void collectNullableMethods(DetailAST node) {
        for (DetailAST child = node; child != null; child = child.getNextSibling()) {
            if (child.getType() == METHOD_DEF && hasAnyAnnotationNamed(child, NULLABLE)) {
                DetailAST ident = child.findFirstToken(IDENT);
                if (ident != null) {
                    nullableMethodNames.add(ident.getText());
                }
            }
            DetailAST firstChild = child.getFirstChild();
            if (firstChild != null) {
                collectNullableMethods(firstChild);
            }
        }
    }

    private static DetailAST findEnclosingMethod(DetailAST node) {
        for (DetailAST parent = node.getParent(); parent != null; parent = parent.getParent()) {
            int type = parent.getType();
            if (type == METHOD_DEF) {
                return parent;
            }
            if (type == LAMBDA || type == CLASS_DEF || type == RECORD_DEF
                || type == ENUM_DEF || type == INTERFACE_DEF) {
                return null;
            }
        }
        return null;
    }

    private static String extractMethodName(DetailAST methodCall) {
        DetailAST firstChild = methodCall.getFirstChild();
        if (firstChild.getType() == IDENT) {
            return firstChild.getText();
        }
        if (firstChild.getType() == DOT) {
            DetailAST qualifier = firstChild.getFirstChild();
            if (qualifier != null && qualifier.getType() == LITERAL_THIS) {
                DetailAST ident = qualifier.getNextSibling();
                if (ident != null && ident.getType() == IDENT) {
                    return ident.getText();
                }
            }
        }
        return null;
    }
}
