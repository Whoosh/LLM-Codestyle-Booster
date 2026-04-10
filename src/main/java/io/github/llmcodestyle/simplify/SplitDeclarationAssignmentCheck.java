package io.github.llmcodestyle.simplify;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import io.github.llmcodestyle.utils.AstSingleUseUtil;

import static com.puppycrawl.tools.checkstyle.api.TokenTypes.*;

import java.util.List;
import java.util.Set;

/**
 * Detects local variables declared without an initializer that are subsequently assigned
 * unconditionally in the same block. Suggests merging declaration and assignment to a single line.
 *
 * <p>Safe transformation requirements:
 * <ul>
 *   <li>The declaration has no initializer.</li>
 *   <li>A later statement in the SAME block is a plain {@code var = expr;} assignment.</li>
 *   <li>Between the declaration and the assignment there are no references to the variable
 *       (neither read nor write) and no control-flow constructs.</li>
 * </ul>
 */
public class SplitDeclarationAssignmentCheck extends AbstractCheck {

    /**
     * Violation message key.
     */
    static final String MSG_KEY = "split.declaration.assignment";
    private static final int[] TOKENS = {SLIST};
    private static final Set<Integer> CONTROL_FLOW_TYPES = Set.of(
        LITERAL_IF,
        LITERAL_FOR,
        LITERAL_WHILE,
        LITERAL_DO,
        LITERAL_SWITCH,
        LITERAL_TRY,
        LITERAL_RETURN,
        LITERAL_THROW,
        LITERAL_BREAK,
        LITERAL_CONTINUE,
        SLIST,
        LITERAL_SYNCHRONIZED);

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
        List<DetailAST> stmts = AstSingleUseUtil.collectStatements(slist);
        for (int i = 0; i < stmts.size(); i++) {
            DetailAST stmt = stmts.get(i);
            String varName = uninitializedVarName(stmt);
            if (varName == null) {
                continue;
            }
            if (findCleanAssignmentBetween(stmts, i + 1, varName)) {
                log(stmt.getLineNo(), stmt.getColumnNo(), MSG_KEY, varName);
            }
        }
    }

    private static String uninitializedVarName(DetailAST stmt) {
        if (stmt.getType() != VARIABLE_DEF || stmt.findFirstToken(ASSIGN) != null) {
            return null;
        }
        DetailAST ident = stmt.findFirstToken(IDENT);
        return ident != null ? ident.getText() : null;
    }

    private static boolean findCleanAssignmentBetween(List<DetailAST> stmts, int from, String varName) {
        for (int j = from; j < stmts.size(); j++) {
            DetailAST candidate = stmts.get(j);
            if (CONTROL_FLOW_TYPES.contains(candidate.getType())) {
                return false;
            }
            if (isPlainAssignmentTo(candidate, varName)) {
                return true;
            }
            if (AstSingleUseUtil.countIdent(candidate, varName) > 0) {
                return false;
            }
        }
        return false;
    }

    private static boolean isPlainAssignmentTo(DetailAST stmt, String varName) {
        if (stmt.getType() != EXPR) {
            return false;
        }
        DetailAST assign = stmt.getFirstChild();
        if (assign == null || assign.getType() != ASSIGN) {
            return false;
        }
        DetailAST lhs = assign.getFirstChild();
        return lhs != null && lhs.getType() == IDENT && varName.equals(lhs.getText());
    }
}
