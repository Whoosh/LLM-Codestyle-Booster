package io.github.llmcodestyle.simplify;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import io.github.llmcodestyle.utils.AstSingleUseUtil;

import static com.puppycrawl.tools.checkstyle.api.TokenTypes.*;

import java.util.List;

/**
 * Detects {@code boolean} locals initialized with a literal that are flipped by a single
 * conditional assignment immediately afterwards. Suggests collapsing into a direct assignment
 * from the condition expression.
 *
 * <p>Patterns covered:
 * <pre>{@code
 * boolean x = false;
 * if (cond) { x = true; }    // -> boolean x = cond;
 *
 * boolean x = true;
 * if (cond) { x = false; }   // -> boolean x = !cond;
 * }</pre>
 */
public class BooleanFromConditionCheck extends AbstractCheck {

    /**
     * Violation message key.
     */
    static final String MSG_KEY = "boolean.from.condition";
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
        List<DetailAST> stmts = AstSingleUseUtil.collectStatements(slist);
        for (int i = 0; i < stmts.size() - 1; i++) {
            DetailAST decl = stmts.get(i);
            String varName = booleanLiteralVarName(decl);
            if (varName != null && isFlipAssignmentIf(stmts.get(i + 1), varName, literalKindOfInit(decl) == LITERAL_TRUE ? LITERAL_FALSE : LITERAL_TRUE)) {
                log(decl.getLineNo(), decl.getColumnNo(), MSG_KEY, varName);
            }
        }
    }

    private static String booleanLiteralVarName(DetailAST stmt) {
        if (stmt.getType() != VARIABLE_DEF) {
            return null;
        }
        DetailAST type = stmt.findFirstToken(TYPE);
        if (type == null || type.findFirstToken(LITERAL_BOOLEAN) == null) {
            return null;
        }
        if (literalKindOfInit(stmt) == 0) {
            return null;
        }
        DetailAST ident = stmt.findFirstToken(IDENT);
        return ident != null ? ident.getText() : null;
    }

    private static int literalKindOfInit(DetailAST varDef) {
        DetailAST literal = firstChildOfInitializerExpression(varDef);
        if (literal == null) {
            return 0;
        }
        int kind = literal.getType();
        return kind == LITERAL_TRUE || kind == LITERAL_FALSE ? kind : 0;
    }

    private static DetailAST firstChildOfInitializerExpression(DetailAST varDef) {
        DetailAST assign = varDef.findFirstToken(ASSIGN);
        if (assign == null) {
            return null;
        }
        DetailAST expr = assign.findFirstToken(EXPR);
        return expr == null ? null : expr.getFirstChild();
    }

    private static boolean isFlipAssignmentIf(DetailAST stmt, String varName, int expectedLiteralType) {
        if (stmt.getType() != LITERAL_IF || stmt.findFirstToken(LITERAL_ELSE) != null) {
            return false;
        }
        return matchesLiteralAssignment(extractSingleBodyStatement(stmt), varName, expectedLiteralType);
    }

    private static DetailAST extractSingleBodyStatement(DetailAST ifAst) {
        DetailAST rparen = ifAst.findFirstToken(RPAREN);
        return rparen == null ? null : unwrapSingleStatementBody(rparen.getNextSibling());
    }

    private static DetailAST unwrapSingleStatementBody(DetailAST body) {
        if (body == null) {
            return null;
        }
        if (body.getType() == EXPR) {
            return body;
        }
        if (body.getType() != SLIST) {
            return null;
        }
        List<DetailAST> innerStmts = AstSingleUseUtil.collectStatements(body);
        return innerStmts.size() == 1 ? innerStmts.get(0) : null;
    }

    private static boolean matchesLiteralAssignment(DetailAST exprStmt, String varName, int expectedLiteralType) {
        if (exprStmt == null || exprStmt.getType() != EXPR) {
            return false;
        }
        DetailAST assign = exprStmt.getFirstChild();
        if (assign == null || assign.getType() != ASSIGN) {
            return false;
        }
        DetailAST lhs = assign.getFirstChild();
        DetailAST rhs = lhs == null ? null : lhs.getNextSibling();
        if (lhs == null || rhs == null || lhs.getType() != IDENT) {
            return false;
        }
        return varName.equals(lhs.getText()) && rhs.getType() == expectedLiteralType;
    }
}
