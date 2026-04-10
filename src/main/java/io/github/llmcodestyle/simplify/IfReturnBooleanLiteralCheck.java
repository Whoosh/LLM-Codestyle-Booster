package io.github.llmcodestyle.simplify;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;

import static com.puppycrawl.tools.checkstyle.api.TokenTypes.*;
import static io.github.llmcodestyle.utils.AstSingleUseUtil.*;

import java.util.List;

/**
 * Detects an {@code if} that returns a boolean literal followed by a fall-through
 * {@code return} of the opposite literal:
 * <pre>{@code
 * if (cond) {
 *     return true;
 * }
 * return false;
 * }</pre>
 * Suggests collapsing to {@code return cond;} (or {@code return !cond;} for the mirror form).
 */
public class IfReturnBooleanLiteralCheck extends AbstractCheck {

    /**
     * Violation message key.
     */
    static final String MSG_KEY = "if.return.boolean.literal";
    private static final int[] LITERAL_IF_TOKEN = {LITERAL_IF};

    @Override
    public int[] getDefaultTokens() {
        return LITERAL_IF_TOKEN.clone();
    }

    @Override
    public int[] getAcceptableTokens() {
        return LITERAL_IF_TOKEN.clone();
    }

    @Override
    public int[] getRequiredTokens() {
        return LITERAL_IF_TOKEN.clone();
    }

    @Override
    public void visitToken(DetailAST ifAst) {
        if (ifAst.findFirstToken(LITERAL_ELSE) != null) {
            return;
        }
        int thenLiteralKind = singleReturnLiteralKind(extractIfBody(ifAst));
        if (thenLiteralKind == 0) {
            return;
        }
        DetailAST tailReturn = followingReturnInBlock(ifAst);
        if (tailReturn == null) {
            return;
        }
        int tailLiteralKind = literalKindOfReturn(tailReturn);
        if (tailLiteralKind != 0 && tailLiteralKind != thenLiteralKind) {
            log(ifAst.getLineNo(), ifAst.getColumnNo(), MSG_KEY);
        }
    }

    private static DetailAST extractIfBody(DetailAST ifAst) {
        DetailAST rparen = ifAst.findFirstToken(RPAREN);
        return rparen != null ? rparen.getNextSibling() : null;
    }

    private static DetailAST followingReturnInBlock(DetailAST ifAst) {
        DetailAST sibling = ifAst.getNextSibling();
        while (sibling != null) {
            int siblingType = sibling.getType();
            if (siblingType == LITERAL_RETURN) {
                return sibling;
            }
            if (siblingType != SEMI && siblingType != RCURLY) {
                return null;
            }
            sibling = sibling.getNextSibling();
        }
        return null;
    }

    private static int singleReturnLiteralKind(DetailAST body) {
        if (body == null) {
            return 0;
        }
        if (body.getType() == LITERAL_RETURN) {
            return literalKindOfReturn(body);
        }
        if (body.getType() != SLIST) {
            return 0;
        }
        List<DetailAST> innerStmts = collectStatements(body);
        if (innerStmts.size() != 1) {
            return 0;
        }
        DetailAST inner = innerStmts.get(0);
        return inner.getType() == LITERAL_RETURN ? literalKindOfReturn(inner) : 0;
    }

    private static int literalKindOfReturn(DetailAST returnAst) {
        DetailAST expr = returnAst.findFirstToken(EXPR);
        if (expr == null) {
            return 0;
        }
        DetailAST first = expr.getFirstChild();
        if (first == null) {
            return 0;
        }
        if (first.getType() == LITERAL_TRUE || first.getType() == LITERAL_FALSE) {
            return first.getType();
        }
        return 0;
    }
}
