package io.github.llmcodestyle.simplify;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;

import static com.puppycrawl.tools.checkstyle.api.TokenTypes.*;

/**
 * Detects if-else blocks where both branches are a single {@code return} of a simple expression.
 * Suggests collapsing to a ternary: {@code return cond ? a : b;}.
 */
public class ConditionalReturnToTernaryCheck extends AbstractCheck {

    /**
     * Violation message key.
     */
    static final String MSG_KEY = "conditional.return.to.ternary";
    private static final int[] TOKENS = {LITERAL_IF};

    private static final int MAX_EXPR_DEPTH = 3;

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
    public void visitToken(DetailAST ifAst) {
        if (isElseIf(ifAst)) {
            return;
        }
        DetailAST elseAst = ifAst.findFirstToken(LITERAL_ELSE);
        if (elseAst == null) {
            return;
        }
        if (elseAst.findFirstToken(LITERAL_IF) != null) {
            return;
        }
        DetailAST ifReturn = findSingleReturn(ifAst.findFirstToken(SLIST));
        DetailAST elseReturn = findSingleReturn(elseAst.findFirstToken(SLIST));
        if (ifReturn == null || elseReturn == null) {
            return;
        }
        if (isSimpleExpr(ifReturn) && isSimpleExpr(elseReturn)) {
            log(ifAst, MSG_KEY);
        }
    }

    private static boolean isElseIf(DetailAST ifAst) {
        DetailAST parent = ifAst.getParent();
        return parent != null && parent.getType() == LITERAL_ELSE;
    }

    private static DetailAST findSingleReturn(DetailAST slist) {
        if (slist == null) {
            return null;
        }
        DetailAST returnAst = null;
        int count = 0;
        for (DetailAST child = slist.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child.getType() == RCURLY || child.getType() == SEMI) {
                continue;
            }
            count++;
            if (child.getType() == LITERAL_RETURN) {
                returnAst = child;
            }
        }
        return count == 1 && returnAst != null ? returnAst : null;
    }

    private static boolean isSimpleExpr(DetailAST returnAst) {
        DetailAST expr = returnAst.findFirstToken(EXPR);
        return expr != null && !containsType(expr, QUESTION) && depth(expr) <= MAX_EXPR_DEPTH;
    }

    private static boolean containsType(DetailAST node, int type) {
        if (node.getType() == type) {
            return true;
        }
        for (DetailAST child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (containsType(child, type)) {
                return true;
            }
        }
        return false;
    }

    private static int depth(DetailAST node) {
        int max = 0;
        for (DetailAST child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
            max = Math.max(max, depth(child));
        }
        return max + 1;
    }
}
