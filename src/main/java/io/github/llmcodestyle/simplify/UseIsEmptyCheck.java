package io.github.llmcodestyle.simplify;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;

import static com.puppycrawl.tools.checkstyle.api.TokenTypes.DOT;
import static com.puppycrawl.tools.checkstyle.api.TokenTypes.ELIST;
import static com.puppycrawl.tools.checkstyle.api.TokenTypes.EQUAL;
import static com.puppycrawl.tools.checkstyle.api.TokenTypes.EXPR;
import static com.puppycrawl.tools.checkstyle.api.TokenTypes.GE;
import static com.puppycrawl.tools.checkstyle.api.TokenTypes.GT;
import static com.puppycrawl.tools.checkstyle.api.TokenTypes.LE;
import static com.puppycrawl.tools.checkstyle.api.TokenTypes.LT;
import static com.puppycrawl.tools.checkstyle.api.TokenTypes.METHOD_CALL;
import static com.puppycrawl.tools.checkstyle.api.TokenTypes.NOT_EQUAL;
import static com.puppycrawl.tools.checkstyle.api.TokenTypes.NUM_INT;

/**
 * Flags {@code length()} / {@code size()} comparisons replaceable with {@code isEmpty()}.
 */
public class UseIsEmptyCheck extends AbstractCheck {

    /**
     * Violation message key.
     */
    static final String MSG_KEY = "use.isEmpty";

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
        return new int[] {GT, GE, LT, LE, EQUAL, NOT_EQUAL};
    }

    @Override
    public void visitToken(DetailAST ast) {
        DetailAST left = ast.getFirstChild();
        DetailAST right = left != null ? left.getNextSibling() : null;
        if (left == null || right == null) {
            return;
        }
        if (isSizeOrLengthCall(left) && isSmallInt(right) && isReplaceable(ast.getType(), intValue(right))) {
            log(ast.getLineNo(), ast.getColumnNo(), MSG_KEY, methodName(left));
        } else if (isSizeOrLengthCall(right) && isSmallInt(left) && isReplaceableReversed(ast.getType(), intValue(left))) {
            log(ast.getLineNo(), ast.getColumnNo(), MSG_KEY, methodName(right));
        }
    }

    private static boolean isReplaceable(int op, int value) {
        return isReplaceableZero(op, value) || isReplaceableOne(op, value);
    }

    private static boolean isReplaceableZero(int op, int value) {
        return value == 0 && (op == GT || op == NOT_EQUAL || op == EQUAL);
    }

    private static boolean isReplaceableOne(int op, int value) {
        return value == 1 && (op == GE || op == LT);
    }

    private static boolean isReplaceableReversed(int op, int value) {
        return isReplaceableReversedZero(op, value) || isReplaceableReversedOne(op, value);
    }

    private static boolean isReplaceableReversedZero(int op, int value) {
        return value == 0 && (op == LT || op == NOT_EQUAL || op == EQUAL);
    }

    private static boolean isReplaceableReversedOne(int op, int value) {
        return value == 1 && (op == LE || op == GT);
    }

    private static boolean isSizeOrLengthCall(DetailAST node) {
        DetailAST n = unwrap(node);
        if (n == null || n.getType() != METHOD_CALL) {
            return false;
        }
        DetailAST dot = n.findFirstToken(DOT);
        if (dot == null) {
            return false;
        }
        DetailAST methodIdent = dot.getLastChild();
        if (methodIdent == null) {
            return false;
        }
        String name = methodIdent.getText();
        if (!"length".equals(name) && !"size".equals(name)) {
            return false;
        }
        DetailAST elist = n.findFirstToken(ELIST);
        return elist != null && elist.getChildCount() == 0;
    }

    private static String methodName(DetailAST node) {
        DetailAST n = unwrap(node);
        if (n == null || n.getType() != METHOD_CALL) {
            return "?";
        }
        DetailAST dot = n.findFirstToken(DOT);
        if (dot == null) {
            return "?";
        }
        DetailAST methodIdent = dot.getLastChild();
        return methodIdent != null ? methodIdent.getText() : "?";
    }

    private static boolean isSmallInt(DetailAST node) {
        DetailAST n = unwrap(node);
        return n != null && n.getType() == NUM_INT && ("0".equals(n.getText()) || "1".equals(n.getText()));
    }

    private static int intValue(DetailAST node) {
        DetailAST n = unwrap(node);
        if (n != null && n.getType() == NUM_INT) {
            return Integer.parseInt(n.getText());
        }
        return -1;
    }

    private static DetailAST unwrap(DetailAST node) {
        if (node != null && node.getType() == EXPR) {
            return node.getFirstChild();
        }
        return node;
    }
}
