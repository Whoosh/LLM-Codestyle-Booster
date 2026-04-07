package io.github.llmcodestyle.simplify;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import io.github.llmcodestyle.utils.AstUtil;

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
    private static final int[] TOKENS = {METHOD_CALL};

    private static final Set<String> REPLACEABLE_METHODS = Set.of("emptyList", "emptySet", "emptyMap", "singletonList", "singleton", "singletonMap");

    private static final Set<String> UNMODIFIABLE_METHODS = Set.of("unmodifiableList", "unmodifiableSet", "unmodifiableMap");

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
        if (!"Collections".equals(AstUtil.extractReceiverName(ast))) {
            return;
        }
        String methodName = AstUtil.extractMethodName(ast);
        if (methodName.isEmpty()) {
            return;
        }

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
        return "Arrays".equals(AstUtil.extractReceiverName(innerCall)) && "asList".equals(AstUtil.extractMethodName(innerCall));
    }
}
