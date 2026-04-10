package io.github.llmcodestyle.simplify;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import io.github.llmcodestyle.utils.AstQueryUtil;
import io.github.llmcodestyle.utils.AstSingleUseUtil;

import static com.puppycrawl.tools.checkstyle.api.TokenTypes.*;

import java.util.List;

/**
 * Detects nested {@code if} statements that can be collapsed using a single boolean AND:
 * <pre>{@code
 * if (a) {
 *     if (b) {
 *         ...
 *     }
 * }
 * }</pre>
 * can be rewritten as {@code if (a && b) { ... }}. Safe when neither if has an {@code else}
 * branch and the outer block contains nothing besides the inner if.
 */
public class CollapsibleNestedIfCheck extends AbstractCheck {

    /**
     * Violation message key.
     */
    static final String MSG_KEY = "collapsible.nested.if";
    private static final int[] TOKENS = {LITERAL_IF};

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
    public void visitToken(DetailAST outerIf) {
        if (AstQueryUtil.isElseIf(outerIf) || hasElseClause(outerIf)) {
            return;
        }
        DetailAST outerBody = extractBlockBody(outerIf);
        if (outerBody == null) {
            return;
        }
        DetailAST innerIf = singleChildIf(outerBody);
        if (innerIf == null || hasElseClause(innerIf)) {
            return;
        }
        log(outerIf.getLineNo(), outerIf.getColumnNo(), MSG_KEY);
    }

    private static boolean hasElseClause(DetailAST ifAst) {
        return ifAst.findFirstToken(LITERAL_ELSE) != null;
    }

    private static DetailAST extractBlockBody(DetailAST ifAst) {
        DetailAST rparen = ifAst.findFirstToken(RPAREN);
        if (rparen == null) {
            return null;
        }
        DetailAST after = rparen.getNextSibling();
        return after != null && after.getType() == SLIST ? after : null;
    }

    private static DetailAST singleChildIf(DetailAST slist) {
        List<DetailAST> stmts = AstSingleUseUtil.collectStatements(slist);
        if (stmts.size() != 1) {
            return null;
        }
        DetailAST only = stmts.get(0);
        return only.getType() == LITERAL_IF ? only : null;
    }
}
