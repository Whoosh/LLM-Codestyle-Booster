package io.github.llmcodestyle.simplify;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import io.github.llmcodestyle.utils.AstMethodCallUtil;
import io.github.llmcodestyle.utils.AstQueryUtil;

import static com.puppycrawl.tools.checkstyle.api.TokenTypes.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Detects chains of {@code ||} where every operand is either
 * <ul>
 *   <li>an equality comparison {@code expr == LITERAL} against literals of a primitive-comparable
 *       type ({@code int}, {@code long}, {@code float}, {@code double}, {@code char}), with a
 *       structurally identical left-hand side across all operands; or</li>
 *   <li>a {@code receiver.equals(STRING_LITERAL)} call with the same receiver text across
 *       all operands.</li>
 * </ul>
 * When at least {@code minOperands} (default 3) consecutive operands in a {@code LOR} chain
 * fit one of those two shapes, the check suggests extracting the literal list into a
 * {@code static final Set<>} constant and replacing the chain with {@code SET.contains(expr)}.
 *
 * <p>Conservative scope: chains with mixed shapes, different left-hand sides, or literals of
 * different categories are ignored. String equality via {@code ==} is never flagged (PMD
 * already covers that). Parenthesized or nested-in-a-larger-boolean operands are handled: the
 * check only processes the topmost {@code LOR} of each chain, flattening any nested {@code LOR}
 * children into a single operand list.
 */
public class OrChainToSetContainsCheck extends AbstractCheck {

    /**
     * Violation message key.
     */
    static final String MSG_KEY = "or.chain.to.set.contains";
    private static final int[] TOKENS = {LOR};
    private static final int DEFAULT_MIN_OPERANDS = 3;

    private static final Set<Integer> COMPARABLE_LITERALS = Set.of(NUM_INT, NUM_LONG, NUM_FLOAT, NUM_DOUBLE, CHAR_LITERAL);

    private int minOperands = DEFAULT_MIN_OPERANDS;

    /**
     * Sets the minimum number of operands in a {@code ||} chain to trigger a violation.
     */
    public void setMinOperands(int minOperands) {
        this.minOperands = minOperands;
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
    public void visitToken(DetailAST lor) {
        if (lor.getParent() != null && lor.getParent().getType() == LOR) {
            return;
        }
        List<DetailAST> operands = new ArrayList<>();
        flatten(lor, operands);
        if (operands.size() < minOperands) {
            return;
        }
        String name = detectEqualityChain(operands);
        if (name == null) {
            name = detectEqualsCallChain(operands);
        }
        if (name != null) {
            log(lor.getLineNo(), lor.getColumnNo(), MSG_KEY, name, operands.size());
        }
    }

    private static void flatten(DetailAST node, List<DetailAST> out) {
        if (node.getType() == LOR) {
            flatten(node.getFirstChild(), out);
            flatten(node.getLastChild(), out);
        } else {
            out.add(node);
        }
    }

    /**
     * Returns a rendered LHS text if every operand is {@code LHS == LITERAL} with a structurally
     * identical {@code LHS} and a primitive-comparable literal RHS. Returns {@code null} otherwise.
     */
    private static String detectEqualityChain(List<DetailAST> operands) {
        DetailAST canonicalLhs = null;
        for (DetailAST op : operands) {
            if (op.getType() != EQUAL) {
                return null;
            }
            DetailAST lhs = extractLhsWithLiteralRhs(op);
            if (lhs == null) {
                return null;
            }
            if (canonicalLhs == null) {
                canonicalLhs = lhs;
            } else if (!AstQueryUtil.structurallyEqual(canonicalLhs, lhs)) {
                return null;
            }
        }
        return renderExpression(canonicalLhs);
    }

    /**
     * If {@code equal} is {@code LHS == LITERAL_OR_CONSTANT} (in either order), returns the
     * LHS subtree. Otherwise returns {@code null}. Accepted RHS shapes: primitive-comparable
     * literals (int/long/float/double/char) and simple {@code UPPER_SNAKE_CASE} identifiers
     * (the Java convention for {@code static final} constants).
     */
    private static DetailAST extractLhsWithLiteralRhs(DetailAST equal) {
        DetailAST first = equal.getFirstChild();
        if (first == null) {
            return null;
        }
        DetailAST second = first.getNextSibling();
        if (second == null) {
            return null;
        }
        if (isLiteralOrConstant(second)) {
            return first;
        }
        if (isLiteralOrConstant(first)) {
            return second;
        }
        return null;
    }

    private static boolean isLiteralOrConstant(DetailAST node) {
        return COMPARABLE_LITERALS.contains(node.getType()) || node.getType() == IDENT && isUpperSnakeCase(node.getText());
    }

    private static boolean isUpperSnakeCase(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }
        boolean hasLetter = false;
        for (int i = 0; i < name.length(); i++) {
            char ch = name.charAt(i);
            if (Character.isLetter(ch)) {
                if (Character.isLowerCase(ch)) {
                    return false;
                }
                hasLetter = true;
            } else if (ch != '_' && !Character.isDigit(ch)) {
                return false;
            }
        }
        return hasLetter;
    }

    /**
     * Returns the shared receiver name if every operand is {@code receiver.equals(STRING_LITERAL)}
     * with the same receiver. Returns {@code null} otherwise.
     */
    private static String detectEqualsCallChain(List<DetailAST> operands) {
        String receiverName = null;
        for (DetailAST op : operands) {
            if (op.getType() != METHOD_CALL || !"equals".equals(AstMethodCallUtil.extractMethodName(op))) {
                return null;
            }
            String receiver = AstMethodCallUtil.extractReceiverName(op);
            if (receiver.isEmpty() || !isSingleStringLiteralArg(op)) {
                return null;
            }
            if (receiverName == null) {
                receiverName = receiver;
            } else if (!receiverName.equals(receiver)) {
                return null;
            }
        }
        return receiverName;
    }

    private static boolean isSingleStringLiteralArg(DetailAST methodCall) {
        DetailAST elist = methodCall.findFirstToken(ELIST);
        if (elist == null || elist.getChildCount(EXPR) != 1) {
            return false;
        }
        DetailAST expr = elist.findFirstToken(EXPR);
        if (expr == null) {
            return false;
        }
        DetailAST arg = expr.getFirstChild();
        return arg != null && arg.getType() == STRING_LITERAL && arg.getNextSibling() == null;
    }

    /**
     * Renders a small AST subtree into a compact textual form for diagnostic messages.
     * Handles simple idents, dotted paths, and {@code something.foo()} shapes; falls back
     * to the root node's own text for anything else.
     */
    private static String renderExpression(DetailAST node) {
        if (node == null) {
            return "?";
        }
        if (node.getType() == IDENT) {
            return node.getText();
        }
        if (node.getType() == DOT) {
            return renderExpression(node.getFirstChild()) + "." + renderExpression(node.getLastChild());
        }
        if (node.getType() == METHOD_CALL) {
            return renderExpression(node.getFirstChild()) + "()";
        }
        if (node.getType() == LITERAL_THIS) {
            return "this";
        }
        return node.getText() == null ? "?" : node.getText();
    }
}
