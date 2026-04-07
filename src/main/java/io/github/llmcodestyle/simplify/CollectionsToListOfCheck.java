package io.github.llmcodestyle.simplify;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;

import static com.puppycrawl.tools.checkstyle.api.TokenTypes.*;

import java.util.Set;

/**
 * Detects pre-Java-9 {@code Collections.emptyList()}, {@code Collections.singletonList()},
 * and {@code Collections.unmodifiableList(Arrays.asList(...))} patterns.
 * Suggests using {@code List.of()}, {@code Set.of()}, {@code Map.of()}.
 */
public class CollectionsToListOfCheck extends AbstractCheck {

    /**
     * Violation message key.
     */
    static final String MSG_KEY = "collections.to.list.of";

    private static final Set<String> REPLACEABLE_METHODS = Set.of("emptyList", "emptySet", "emptyMap", "singletonList", "singleton", "singletonMap");

    private static final Set<String> UNMODIFIABLE_METHODS = Set.of("unmodifiableList", "unmodifiableSet", "unmodifiableMap");

    @Override
    public int[] getDefaultTokens() {
        return new int[] {METHOD_CALL};
    }

    @Override
    public int[] getAcceptableTokens() {
        return new int[] {METHOD_CALL};
    }

    @Override
    public int[] getRequiredTokens() {
        return new int[] {METHOD_CALL};
    }

    @Override
    public void visitToken(DetailAST ast) {
        DetailAST dot = ast.findFirstToken(DOT);
        if (dot == null) {
            return;
        }
        DetailAST receiver = dot.getFirstChild();
        DetailAST method = dot.getLastChild();
        if (receiver == null || method == null || receiver.getType() != IDENT || method.getType() != IDENT) {
            return;
        }
        if (!"Collections".equals(receiver.getText())) {
            return;
        }
        String methodName = method.getText();

        if (REPLACEABLE_METHODS.contains(methodName)) {
            log(ast, MSG_KEY, methodName);
        } else if (UNMODIFIABLE_METHODS.contains(methodName) && isWrappingArraysAsList(ast)) {
            log(ast, MSG_KEY, methodName + "(Arrays.asList(...))");
        }
    }

    private static boolean isWrappingArraysAsList(DetailAST methodCall) {
        DetailAST elist = methodCall.findFirstToken(ELIST);
        if (elist == null) {
            return false;
        }
        DetailAST firstExpr = elist.findFirstToken(EXPR);
        if (firstExpr == null) {
            return false;
        }
        DetailAST innerCall = firstExpr.findFirstToken(METHOD_CALL);
        if (innerCall == null) {
            return false;
        }
        DetailAST innerDot = innerCall.findFirstToken(DOT);
        if (innerDot == null) {
            return false;
        }
        DetailAST innerReceiver = innerDot.getFirstChild();
        DetailAST innerMethod = innerDot.getLastChild();
        if (innerReceiver == null || innerMethod == null) {
            return false;
        }
        return "Arrays".equals(innerReceiver.getText()) && "asList".equals(innerMethod.getText());
    }
}
