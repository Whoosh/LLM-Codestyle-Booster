package io.github.llmcodestyle.layout;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;

import static com.puppycrawl.tools.checkstyle.api.TokenTypes.*;

/**
 * Enforces consistency in multi-line method call argument formatting. All args must be on one line or one-per-line — mixed format is forbidden.
 */
public class MethodCallArgumentsOnSameLineCheck extends AbstractCheck {

    /**
     * Violation message key.
     */
    static final String MSG_KEY = "method.call.args.line.consistency";

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
        return new int[] {METHOD_CALL, LITERAL_NEW};
    }

    @Override
    public void visitToken(DetailAST ast) {
        DetailAST elist = ast.findFirstToken(ELIST);
        if (elist == null) {
            return;
        }
        int[] argLines = collectArgumentLines(elist);
        if (argLines.length < 2) {
            return;
        }
        int firstArgLine = argLines[0];
        boolean allSameLine = true;
        for (int i = 1; i < argLines.length; i++) {
            if (argLines[i] != firstArgLine) {
                allSameLine = false;
                break;
            }
        }
        if (allSameLine) {
            return;
        }
        boolean allDifferentLines = true;
        for (int i = 1; i < argLines.length; i++) {
            if (argLines[i] == argLines[i - 1]) {
                allDifferentLines = false;
                break;
            }
        }
        if (allDifferentLines) {
            return;
        }
        log(ast.getLineNo(), ast.getColumnNo(), MSG_KEY);
    }

    private static int[] collectArgumentLines(DetailAST elist) {
        int argCount = 0;
        DetailAST child = elist.getFirstChild();
        while (child != null) {
            if (child.getType() == EXPR) {
                argCount++;
            }
            child = child.getNextSibling();
        }
        int[] argLines = new int[argCount];
        int idx = 0;
        child = elist.getFirstChild();
        while (child != null) {
            if (child.getType() == EXPR) {
                argLines[idx] = child.getLineNo();
                idx++;
            }
            child = child.getNextSibling();
        }
        return argLines;
    }
}
