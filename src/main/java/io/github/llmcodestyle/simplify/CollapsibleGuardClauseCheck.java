package io.github.llmcodestyle.simplify;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import io.github.llmcodestyle.utils.AstSingleUseUtil;

import static com.puppycrawl.tools.checkstyle.api.TokenTypes.*;

import java.util.List;

/**
 * Detects an early-return guard followed by a single conditional that could be safely collapsed:
 * <pre>{@code
 * if (a) { return; }
 * if (b) { ... }
 * }</pre>
 * can be rewritten as {@code if (!a && b) { ... }} when both ifs are the only two statements
 * in a method/constructor body. The transformation is safe because both branches are exhaustive
 * and {@code return} returns from the same enclosing scope as the falling-off-the-end behavior.
 */
public class CollapsibleGuardClauseCheck extends AbstractCheck {

    /**
     * Violation message key.
     */
    static final String MSG_KEY = "collapsible.guard.clause";
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
        if (!isMethodOrCtorBody(slist)) {
            return;
        }
        List<DetailAST> stmts = AstSingleUseUtil.collectStatements(slist);
        if (stmts.size() != 2) {
            return;
        }
        DetailAST guard = stmts.get(0);
        DetailAST tail = stmts.get(1);
        if (guard.getType() != LITERAL_IF || tail.getType() != LITERAL_IF || hasElseClause(guard) || hasElseClause(tail) || !isVoidReturnOnly(guard)) {
            return;
        }
        log(guard.getLineNo(), guard.getColumnNo(), MSG_KEY);
    }

    private static boolean isMethodOrCtorBody(DetailAST slist) {
        DetailAST parent = slist.getParent();
        if (parent == null) {
            return false;
        }
        int type = parent.getType();
        return type == METHOD_DEF || type == CTOR_DEF || type == COMPACT_CTOR_DEF;
    }

    private static boolean hasElseClause(DetailAST ifAst) {
        return ifAst.findFirstToken(LITERAL_ELSE) != null;
    }

    private static boolean isVoidReturnOnly(DetailAST guardIf) {
        DetailAST body = extractIfBody(guardIf);
        if (body == null) {
            return false;
        }
        if (body.getType() == LITERAL_RETURN) {
            return body.findFirstToken(EXPR) == null;
        }
        if (body.getType() != SLIST) {
            return false;
        }
        List<DetailAST> innerStmts = AstSingleUseUtil.collectStatements(body);
        if (innerStmts.size() != 1) {
            return false;
        }
        DetailAST single = innerStmts.get(0);
        return single.getType() == LITERAL_RETURN && single.findFirstToken(EXPR) == null;
    }

    private static DetailAST extractIfBody(DetailAST ifAst) {
        DetailAST rparen = ifAst.findFirstToken(RPAREN);
        if (rparen == null) {
            return null;
        }
        DetailAST body = rparen.getNextSibling();
        while (body != null && body.getType() != SLIST && body.getType() != LITERAL_RETURN
            && body.getType() != EXPR && body.getType() != LITERAL_THROW) {
            body = body.getNextSibling();
        }
        return body;
    }
}
