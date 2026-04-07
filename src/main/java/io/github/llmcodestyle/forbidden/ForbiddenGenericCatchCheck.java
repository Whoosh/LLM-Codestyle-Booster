package io.github.llmcodestyle.forbidden;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;

import static com.puppycrawl.tools.checkstyle.api.TokenTypes.BOR;
import static com.puppycrawl.tools.checkstyle.api.TokenTypes.DOT;
import static com.puppycrawl.tools.checkstyle.api.TokenTypes.IDENT;
import static com.puppycrawl.tools.checkstyle.api.TokenTypes.LITERAL_CATCH;
import static com.puppycrawl.tools.checkstyle.api.TokenTypes.PARAMETER_DEF;
import static com.puppycrawl.tools.checkstyle.api.TokenTypes.TYPE;

/** Forbids catching {@code Exception}, {@code Throwable}, or {@code RuntimeException}. Runs alongside PMD's rule for defence-in-depth. */
public class ForbiddenGenericCatchCheck extends AbstractCheck {

    /** Message key for generic catch violations. */
    static final String MSG_KEY = "forbidden.generic.catch";

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
        return new int[]{LITERAL_CATCH};
    }

    @Override
    public void visitToken(DetailAST ast) {
        DetailAST parameter = ast.findFirstToken(PARAMETER_DEF);
        if (parameter == null) {
            return;
        }
        DetailAST type = parameter.findFirstToken(TYPE);
        if (type == null) {
            return;
        }
        checkTypeNode(type);
    }

    private void checkTypeNode(DetailAST typeNode) {
        DetailAST child = typeNode.getFirstChild();
        while (child != null) {
            checkChild(child);
            child = child.getNextSibling();
        }
    }

    private void checkBorNode(DetailAST bor) {
        DetailAST child = bor.getFirstChild();
        while (child != null) {
            checkChild(child);
            child = child.getNextSibling();
        }
    }

    private void checkChild(DetailAST child) {
        if (child.getType() == IDENT) {
            logIfForbidden(child);
        } else if (child.getType() == DOT) {
            DetailAST last = child.getLastChild();
            if (last != null && last.getType() == IDENT) {
                logIfForbidden(last);
            }
        } else if (child.getType() == BOR) {
            checkBorNode(child);
        }
    }

    private void logIfForbidden(DetailAST ident) {
        String name = ident.getText();
        if (isForbidden(name)) {
            log(ident.getLineNo(), ident.getColumnNo(), MSG_KEY, name);
        }
    }

    private static boolean isForbidden(String name) {
        return "Exception".equals(name) || "Throwable".equals(name) || "RuntimeException".equals(name);
    }
}
