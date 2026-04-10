package io.github.llmcodestyle.simplify;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import io.github.llmcodestyle.utils.AstSingleUseUtil;

import static com.puppycrawl.tools.checkstyle.api.TokenTypes.*;

import java.util.List;
import java.util.Set;

/**
 * Flags local variables assigned once and used exactly once in the immediately following statement.
 */
public class SingleUseLocalVariableCheck extends AbstractCheck {

    /**
     * Violation message key.
     */
    static final String MSG_KEY = "single.use.local.variable";
    private static final int[] TOKENS = {SLIST};
    private static final Set<Integer> METHOD_LIKE_PARENTS = Set.of(METHOD_DEF, CTOR_DEF, COMPACT_CTOR_DEF, LAMBDA, STATIC_INIT, INSTANCE_INIT);

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
        if (!isMethodBody(ast)) {
            return;
        }
        checkBlock(ast);
    }

    private void checkBlock(DetailAST slist) {
        List<DetailAST> statements = AstSingleUseUtil.collectStatements(slist);
        for (int i = 0; i < statements.size() - 1; i++) {
            DetailAST stmt = statements.get(i);
            if (stmt.getType() != VARIABLE_DEF || stmt.findFirstToken(ASSIGN) == null) {
                continue;
            }
            DetailAST ident = stmt.findFirstToken(IDENT);
            if (ident == null) {
                continue;
            }
            String varName = ident.getText();
            DetailAST nextStmt = statements.get(i + 1);

            int refsInNext = AstSingleUseUtil.countIdent(nextStmt, varName);
            if (refsInNext == 0) {
                continue;
            }

            int totalRefs = 0;
            for (int j = i + 1; j < statements.size(); j++) {
                totalRefs += AstSingleUseUtil.countIdent(statements.get(j), varName);
            }

            if (totalRefs == 1 && refsInNext == 1 && !AstSingleUseUtil.isInsideRepeatingContext(nextStmt, varName) && !containsIdentInNestedBlock(nextStmt, varName, false)) {
                log(stmt.getLineNo(), stmt.getColumnNo(), MSG_KEY, varName);
            }
        }
    }

    private static boolean containsIdentInNestedBlock(DetailAST node, String varName, boolean insideBlock) {
        if (node.getType() == IDENT && varName.equals(node.getText()) && insideBlock) {
            return true;
        }
        boolean nowInBlock = insideBlock || node.getType() == SLIST;
        DetailAST child = node.getFirstChild();
        while (child != null) {
            if (containsIdentInNestedBlock(child, varName, nowInBlock)) {
                return true;
            }
            child = child.getNextSibling();
        }
        return false;
    }

    private static boolean isMethodBody(DetailAST slist) {
        DetailAST parent = slist.getParent();
        if (parent == null) {
            return false;
        }
        int type = parent.getType();
        return METHOD_LIKE_PARENTS.contains(type) || isControlFlowParent(type);
    }

    private static boolean isControlFlowParent(int type) {
        return AstSingleUseUtil.isLoopOrCondition(type) || AstSingleUseUtil.isExceptionBlock(type);
    }
}
