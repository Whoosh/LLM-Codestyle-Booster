package io.github.llmcodestyle.simplify;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import io.github.llmcodestyle.utils.AstQueryUtil;
import io.github.llmcodestyle.utils.AstSingleUseUtil;

import static com.puppycrawl.tools.checkstyle.api.TokenTypes.*;

import java.util.List;

/**
 * Detects two (or more) consecutive {@code if} statements whose bodies are a single
 * unconditional control-flow statement with structurally identical contents. Such pairs
 * can be merged via {@code ||} without changing semantics:
 * <pre>{@code
 * if (a) return;
 * if (b) return;
 * }</pre>
 * becomes
 * <pre>{@code
 * if (a || b) return;
 * }</pre>
 *
 * <p>The merge is safe if and only if both bodies are a single terminating statement
 * ({@code return;}, {@code return EXPR;}, {@code continue;}, {@code break;}, or
 * {@code throw EXPR;}). For multi-statement or non-terminating bodies the behaviour
 * would change if both conditions were independently true, so this check stays strictly
 * conservative.
 */
public class CollapsibleConsecutiveIfCheck extends AbstractCheck {

    /**
     * Violation message key.
     */
    static final String MSG_KEY = "collapsible.consecutive.if";
    private static final int[] TOKENS = {SLIST};

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
    public void visitToken(DetailAST slist) {
        List<DetailAST> statements = AstSingleUseUtil.collectStatements(slist);
        for (int i = 1; i < statements.size(); i++) {
            DetailAST prev = statements.get(i - 1);
            DetailAST current = statements.get(i);
            DetailAST prevBody = extractTerminatingBody(prev);
            DetailAST currentBody = extractTerminatingBody(current);
            if (prevBody != null && currentBody != null && structurallyEqual(prevBody, currentBody)) {
                log(current.getLineNo(), current.getColumnNo(), MSG_KEY, prev.getLineNo());
            }
        }
    }

    /**
     * Returns the single terminating child of an {@code if} statement's body,
     * or {@code null} if the if has an else, is an else-if, or the body is
     * not a single terminating control-flow statement.
     */
    private static DetailAST extractTerminatingBody(DetailAST stmt) {
        if (stmt.getType() != LITERAL_IF || stmt.findFirstToken(LITERAL_ELSE) != null || AstQueryUtil.isElseIf(stmt)) {
            return null;
        }
        DetailAST rparen = stmt.findFirstToken(RPAREN);
        if (rparen == null) {
            return null;
        }
        DetailAST bodyNode = rparen.getNextSibling();
        if (bodyNode == null) {
            return null;
        }
        DetailAST single = unwrapSingleStatement(bodyNode);
        return single != null && isTerminating(single.getType()) ? single : null;
    }

    /**
     * If {@code body} is a block with exactly one non-punctuation statement, returns that statement.
     * If {@code body} is a single statement directly (no braces), returns it.
     * Otherwise returns {@code null}.
     */
    private static DetailAST unwrapSingleStatement(DetailAST body) {
        if (body.getType() != SLIST) {
            return body;
        }
        List<DetailAST> inner = AstSingleUseUtil.collectStatements(body);
        return inner.size() == 1 ? inner.get(0) : null;
    }

    private static boolean isTerminating(int type) {
        return type == LITERAL_RETURN || type == LITERAL_CONTINUE || type == LITERAL_BREAK || type == LITERAL_THROW;
    }

    /**
     * Structural AST equality: same type, same text, same children in order. Token positions ignored.
     */
    private static boolean structurallyEqual(DetailAST a, DetailAST b) {
        if (a == null || b == null) {
            return a == null && b == null;
        }
        if (a.getType() != b.getType()) {
            return false;
        }
        String ta = a.getText();
        String tb = b.getText();
        if (ta == null ? tb != null : !ta.equals(tb)) {
            return false;
        }
        DetailAST ca = a.getFirstChild();
        DetailAST cb = b.getFirstChild();
        while (ca != null && cb != null) {
            if (!structurallyEqual(ca, cb)) {
                return false;
            }
            ca = ca.getNextSibling();
            cb = cb.getNextSibling();
        }
        return ca == null && cb == null;
    }
}
