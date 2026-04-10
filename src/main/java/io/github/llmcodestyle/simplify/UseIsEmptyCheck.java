package io.github.llmcodestyle.simplify;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import io.github.llmcodestyle.utils.AstMethodCallUtil;

import static com.puppycrawl.tools.checkstyle.api.TokenTypes.*;

import java.util.Set;

/**
 * Flags {@code length()} / {@code size()} comparisons replaceable with {@code isEmpty()}.
 */
public class UseIsEmptyCheck extends AbstractCheck {

    /**
     * Violation message key.
     */
    static final String MSG_KEY = "use.isEmpty";
    private static final int[] TOKENS = {GT, GE, LT, LE, EQUAL, NOT_EQUAL};
    private static final Set<Integer> ZERO_FORWARD_OPS = Set.of(GT, NOT_EQUAL, EQUAL);
    private static final Set<Integer> ZERO_REVERSE_OPS = Set.of(LT, NOT_EQUAL, EQUAL);

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
        if (isSizeOrLengthCall(left) && isSmallInt(right) && isReplaceable(ast.getType(), intValue(right))) {
            log(ast.getLineNo(), ast.getColumnNo(), MSG_KEY, methodName(left));
        } else if (isSizeOrLengthCall(right) && isSmallInt(left) && isReplaceableReversed(ast.getType(), intValue(left))) {
            log(ast.getLineNo(), ast.getColumnNo(), MSG_KEY, methodName(right));
        }
    }

    private static boolean isReplaceable(int op, int value) {
        return value == 0 && ZERO_FORWARD_OPS.contains(op) || isReplaceableOne(op, value);
    }

    private static boolean isReplaceableOne(int op, int value) {
        return value == 1 && (op == GE || op == LT);
    }

    private static boolean isReplaceableReversed(int op, int value) {
        return value == 0 && ZERO_REVERSE_OPS.contains(op) || isReplaceableReversedOne(op, value);
    }

    private static boolean isReplaceableReversedOne(int op, int value) {
        return value == 1 && (op == LE || op == GT);
    }

    private static boolean isSizeOrLengthCall(DetailAST node) {
        DetailAST n = unwrap(node);
        if (n == null || n.getType() != METHOD_CALL) {
            return false;
        }
        String name = AstMethodCallUtil.extractMethodName(n);
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
        String name = AstMethodCallUtil.extractMethodName(n);
        return name.isEmpty() ? "?" : name;
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
