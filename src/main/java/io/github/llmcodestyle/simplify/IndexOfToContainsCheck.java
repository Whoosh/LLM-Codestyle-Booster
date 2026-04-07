package io.github.llmcodestyle.simplify;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import io.github.llmcodestyle.utils.AstUtil;

import static com.puppycrawl.tools.checkstyle.api.TokenTypes.*;

/**
 * Flags {@code indexOf} comparisons that should use {@code contains} instead.
 */
public class IndexOfToContainsCheck extends AbstractCheck {

    /**
     * Violation message key.
     */
    static final String MSG_KEY = "indexof.use.contains";
    private static final int[] TOKENS = {LT, GE, EQUAL, NOT_EQUAL, GT};

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
        DetailAST left = ast.getFirstChild();
        DetailAST right = left != null ? left.getNextSibling() : null;
        if (left == null || right == null) {
            return;
        }
        int type = ast.getType();
        if (isIndexOfCall(left) && isIntLiteralOrUnary(right) && isValidComparison(type, right)) {
            log(ast.getLineNo(), ast.getColumnNo(), MSG_KEY);
        } else if (isIndexOfCall(right) && isIntLiteralOrUnary(left) && isValidComparisonReversed(type, left)) {
            log(ast.getLineNo(), ast.getColumnNo(), MSG_KEY);
        }
    }

    private static boolean isValidComparison(int opType, DetailAST literal) {
        return isZeroComparison(opType, literal) || isMinusOneComparison(opType, literal);
    }

    private static boolean isZeroComparison(int opType, DetailAST literal) {
        return isZero(literal) && (opType == LT || opType == GE);
    }

    private static boolean isMinusOneComparison(int opType, DetailAST literal) {
        return isMinusOne(literal) && (opType == EQUAL || opType == NOT_EQUAL || opType == GT);
    }

    private static boolean isValidComparisonReversed(int opType, DetailAST literal) {
        return isZeroComparisonReversed(opType, literal) || isMinusOneComparisonReversed(opType, literal);
    }

    private static boolean isZeroComparisonReversed(int opType, DetailAST literal) {
        return isZero(literal) && opType == GT;
    }

    private static boolean isMinusOneComparisonReversed(int opType, DetailAST literal) {
        return isMinusOne(literal) && (opType == EQUAL || opType == NOT_EQUAL || opType == LT);
    }

    private static boolean isIndexOfCall(DetailAST expr) {
        if (expr == null) {
            return false;
        }
        DetailAST node = expr.getType() == EXPR ? expr.getFirstChild() : expr;
        if (node == null || node.getType() != METHOD_CALL) {
            return false;
        }
        if (!isIndexOfMethodName(node)) {
            return false;
        }
        DetailAST elist = node.findFirstToken(ELIST);
        if (elist == null || countExprs(elist) != 1) {
            return false;
        }
        return isValidIndexOfArgument(elist.findFirstToken(EXPR));
    }

    private static boolean isIndexOfMethodName(DetailAST methodCall) {
        return "indexOf".equals(AstUtil.extractMethodName(methodCall));
    }

    private static boolean isValidIndexOfArgument(DetailAST firstExpr) {
        if (firstExpr == null || firstExpr.getFirstChild() == null) {
            return true;
        }
        int childType = firstExpr.getFirstChild().getType();
        if (childType == METHOD_CALL || childType == CHAR_LITERAL) {
            return false;
        }
        return childType != IDENT || firstExpr.getFirstChild().getText().length() != 1;
    }

    private static int countExprs(DetailAST elist) {
        int count = 0;
        DetailAST child = elist.getFirstChild();
        while (child != null) {
            if (child.getType() == EXPR) {
                count++;
            }
            child = child.getNextSibling();
        }
        return count;
    }

    private static boolean isZero(DetailAST node) {
        DetailAST n = node.getType() == EXPR ? node.getFirstChild() : node;
        return n != null && n.getType() == NUM_INT && "0".equals(n.getText());
    }

    private static boolean isMinusOne(DetailAST node) {
        DetailAST n = node.getType() == EXPR ? node.getFirstChild() : node;
        if (n == null) {
            return false;
        }
        if (n.getType() == UNARY_MINUS) {
            DetailAST child = n.getFirstChild();
            return child != null && child.getType() == NUM_INT && "1".equals(child.getText());
        }
        return false;
    }

    private static boolean isIntLiteralOrUnary(DetailAST node) {
        DetailAST n = node.getType() == EXPR ? node.getFirstChild() : node;
        if (n == null) {
            return false;
        }
        if (n.getType() == NUM_INT) {
            return true;
        }
        if (n.getType() == UNARY_MINUS) {
            DetailAST child = n.getFirstChild();
            return child != null && child.getType() == NUM_INT;
        }
        return false;
    }
}
