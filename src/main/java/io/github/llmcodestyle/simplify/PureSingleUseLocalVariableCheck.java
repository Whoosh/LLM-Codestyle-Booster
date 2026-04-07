package io.github.llmcodestyle.simplify;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import io.github.llmcodestyle.utils.AstSingleUseUtil;

import static com.puppycrawl.tools.checkstyle.api.TokenTypes.ASSIGN;
import static com.puppycrawl.tools.checkstyle.api.TokenTypes.COMPACT_CTOR_DEF;
import static com.puppycrawl.tools.checkstyle.api.TokenTypes.CTOR_DEF;
import static com.puppycrawl.tools.checkstyle.api.TokenTypes.DEC;
import static com.puppycrawl.tools.checkstyle.api.TokenTypes.DIV_ASSIGN;
import static com.puppycrawl.tools.checkstyle.api.TokenTypes.DOT;
import static com.puppycrawl.tools.checkstyle.api.TokenTypes.EXPR;
import static com.puppycrawl.tools.checkstyle.api.TokenTypes.IDENT;
import static com.puppycrawl.tools.checkstyle.api.TokenTypes.INC;
import static com.puppycrawl.tools.checkstyle.api.TokenTypes.INSTANCE_INIT;
import static com.puppycrawl.tools.checkstyle.api.TokenTypes.LAMBDA;
import static com.puppycrawl.tools.checkstyle.api.TokenTypes.LITERAL_NEW;
import static com.puppycrawl.tools.checkstyle.api.TokenTypes.METHOD_CALL;
import static com.puppycrawl.tools.checkstyle.api.TokenTypes.METHOD_DEF;
import static com.puppycrawl.tools.checkstyle.api.TokenTypes.MINUS_ASSIGN;
import static com.puppycrawl.tools.checkstyle.api.TokenTypes.PLUS_ASSIGN;
import static com.puppycrawl.tools.checkstyle.api.TokenTypes.POST_DEC;
import static com.puppycrawl.tools.checkstyle.api.TokenTypes.POST_INC;
import static com.puppycrawl.tools.checkstyle.api.TokenTypes.SLIST;
import static com.puppycrawl.tools.checkstyle.api.TokenTypes.STAR_ASSIGN;
import static com.puppycrawl.tools.checkstyle.api.TokenTypes.STATIC_INIT;
import static com.puppycrawl.tools.checkstyle.api.TokenTypes.VARIABLE_DEF;

import java.util.List;
import java.util.Set;

/** Flags local variables with pure initializers that are used exactly once in a later (not immediately next) statement. Complements {@link SingleUseLocalVariableCheck}. */
public class PureSingleUseLocalVariableCheck extends AbstractCheck {

    static final String MSG_KEY = "pure.single.use.local.variable";

    private static final Set<String> PURE_METHODS = Set.of(
        "get",
        "size",
        "length",
        "isEmpty",
        "toString",
        "hashCode",
        "equals",
        "charAt",
        "indexOf",
        "lastIndexOf",
        "substring",
        "trim",
        "strip",
        "valueOf",
        "parseInt",
        "parseLong",
        "parseDouble",
        "parseFloat",
        "group",
        "getFirst",
        "getLast",
        "getKey",
        "getValue",
        "getText",
        "getName",
        "getFileName",
        "getType",
        "min",
        "max",
        "abs",
        "toArray",
        "toList",
        "of",
        "copyOf",
        "format",
        "replace",
        "replaceAll",
        "matcher",
        "toLowerCase",
        "toUpperCase",
        "startsWith",
        "endsWith",
        "contains"
    );

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
        return new int[]{SLIST};
    }

    @Override
    public void visitToken(DetailAST ast) {
        if (!isEligibleBlock(ast)) {
            return;
        }
        analyzeBlock(ast);
    }

    private void analyzeBlock(DetailAST slist) {
        analyzeStatements(AstSingleUseUtil.collectStatements(slist));
    }

    private void analyzeStatements(List<DetailAST> statements) {
        for (int i = 0; i < statements.size() - 1; i++) {
            DetailAST stmt = statements.get(i);
            if (stmt.getType() != VARIABLE_DEF) {
                continue;
            }
            analyzeCandidate(statements, i, stmt);
        }
    }

    private void analyzeCandidate(List<DetailAST> statements, int idx, DetailAST stmt) {
        DetailAST assign = stmt.findFirstToken(ASSIGN);
        if (assign == null) {
            return;
        }
        DetailAST ident = stmt.findFirstToken(IDENT);
        if (ident == null) {
            return;
        }
        String varName = ident.getText();

        if (countIdentDirect(statements.get(idx + 1), varName) > 0) {
            return;
        }

        int totalUses = 0;
        for (int j = idx + 1; j < statements.size(); j++) {
            totalUses += AstSingleUseUtil.countIdent(statements.get(j), varName);
        }
        if (totalUses != 1) {
            return;
        }

        if (isUsedInRepeatingContextFrom(statements, idx + 1, varName)) {
            return;
        }

        DetailAST initExpr = assign.findFirstToken(EXPR);
        if (initExpr == null || !isPureExpression(initExpr)) {
            return;
        }

        log(stmt.getLineNo(), stmt.getColumnNo(), MSG_KEY, varName);
    }

    private static boolean isPureExpression(DetailAST node) {
        if (isImpureToken(node.getType())) {
            return false;
        }
        if (node.getType() == METHOD_CALL && !PURE_METHODS.contains(extractMethodName(node))) {
            return false;
        }
        DetailAST child = node.getFirstChild();
        while (child != null) {
            if (!isPureExpression(child)) {
                return false;
            }
            child = child.getNextSibling();
        }
        return true;
    }

    private static boolean isImpureToken(int type) {
        return isCreationOrMutation(type) || isCompoundAssignment(type);
    }

    private static boolean isCreationOrMutation(int type) {
        return type == LITERAL_NEW || type == POST_INC || type == POST_DEC || type == INC || type == DEC || type == ASSIGN;
    }

    private static boolean isCompoundAssignment(int type) {
        return type == PLUS_ASSIGN || type == MINUS_ASSIGN || type == STAR_ASSIGN || type == DIV_ASSIGN;
    }

    private static String extractMethodName(DetailAST methodCall) {
        DetailAST dot = methodCall.findFirstToken(DOT);
        if (dot != null) {
            return dot.getLastChild().getText();
        }
        DetailAST nameIdent = methodCall.findFirstToken(IDENT);
        return nameIdent != null ? nameIdent.getText() : "";
    }

    /** Count IDENT uses NOT inside nested SLIST blocks — top-level uses only. */
    private static int countIdentDirect(DetailAST root, String name) {
        return countIdentExcludingNested(root, name, false);
    }

    private static int countIdentExcludingNested(DetailAST node, String name, boolean insideNested) {
        if (insideNested) {
            return 0;
        }
        int count = 0;
        if (node.getType() == IDENT && name.equals(node.getText())) {
            count++;
        }
        DetailAST child = node.getFirstChild();
        while (child != null) {
            count += countIdentExcludingNested(child, name, child.getType() == SLIST);
            child = child.getNextSibling();
        }
        return count;
    }

    private static boolean isUsedInRepeatingContextFrom(List<DetailAST> statements, int startIdx, String varName) {
        for (int j = startIdx; j < statements.size(); j++) {
            if (AstSingleUseUtil.isInsideRepeatingContext(statements.get(j), varName)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isEligibleBlock(DetailAST slist) {
        DetailAST parent = slist.getParent();
        if (parent == null) {
            return false;
        }
        int type = parent.getType();
        return isMethodLikeBlock(type) || isFlowBlock(type);
    }

    private static boolean isMethodLikeBlock(int type) {
        return type == METHOD_DEF || type == CTOR_DEF || type == COMPACT_CTOR_DEF || type == LAMBDA || type == STATIC_INIT || type == INSTANCE_INIT;
    }

    private static boolean isFlowBlock(int type) {
        return AstSingleUseUtil.isLoopOrCondition(type) || AstSingleUseUtil.isExceptionBlock(type);
    }
}
