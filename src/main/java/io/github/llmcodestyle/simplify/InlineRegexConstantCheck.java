package io.github.llmcodestyle.simplify;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;

import static com.puppycrawl.tools.checkstyle.api.TokenTypes.*;

/**
 * Flags inline regex string literals in methods that should be extracted to static final Pattern constants.
 */
public class InlineRegexConstantCheck extends AbstractCheck {

    private static final String MSG_KEY = "inline.regex.constant";
    private static final int[] TOKENS = {METHOD_CALL};
    private static final int MIN_REGEX_LITERAL_LENGTH = 3;

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
        if (!isInsideMethodBody(ast)) {
            return;
        }

        String methodName = extractMethodName(ast);
        if (methodName == null) {
            return;
        }

        if (isRegexAcceptingMethod(methodName)) {
            checkFirstArgIsStringLiteral(ast, methodName);
        } else if ("compile".equals(methodName) && isPatternCompile(ast)) {
            checkFirstArgIsStringLiteral(ast, "Pattern.compile");
        }
    }

    private static boolean isInsideMethodBody(DetailAST ast) {
        DetailAST parent = ast.getParent();
        while (parent != null) {
            if (parent.getType() == METHOD_DEF || parent.getType() == CTOR_DEF) {
                return true;
            }
            if (parent.getType() == VARIABLE_DEF) {
                DetailAST grandParent = parent.getParent();
                if (grandParent != null && grandParent.getType() == OBJBLOCK) {
                    return false;
                }
            }
            parent = parent.getParent();
        }
        return false;
    }

    private static String extractMethodName(DetailAST methodCall) {
        DetailAST dot = methodCall.findFirstToken(DOT);
        if (dot != null) {
            return dot.getLastChild().getText();
        }
        DetailAST ident = methodCall.findFirstToken(IDENT);
        return ident != null ? ident.getText() : null;
    }

    private static boolean isRegexAcceptingMethod(String name) {
        return "matches".equals(name) || "replaceAll".equals(name) || "replaceFirst".equals(name) || "split".equals(name);
    }

    private static boolean isPatternCompile(DetailAST methodCall) {
        DetailAST dot = methodCall.findFirstToken(DOT);
        if (dot == null) {
            return false;
        }
        DetailAST target = dot.getFirstChild();
        return target != null && "Pattern".equals(target.getText());
    }

    private void checkFirstArgIsStringLiteral(DetailAST methodCall, String methodName) {
        DetailAST elist = methodCall.findFirstToken(ELIST);
        if (elist == null) {
            return;
        }
        DetailAST firstExpr = elist.findFirstToken(EXPR);
        if (firstExpr == null) {
            return;
        }
        DetailAST firstChild = firstExpr.getFirstChild();
        if (firstChild != null && firstChild.getType() == STRING_LITERAL) {
            if (firstChild.getText().length() <= MIN_REGEX_LITERAL_LENGTH) {
                return;
            }
            log(methodCall.getLineNo(), MSG_KEY, methodName);
        }
    }
}
