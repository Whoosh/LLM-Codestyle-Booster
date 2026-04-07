package io.github.llmcodestyle.quality;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import io.github.llmcodestyle.utils.AstAnnotationUtil;

import static com.puppycrawl.tools.checkstyle.api.TokenTypes.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Flags non-private methods whose single-statement body delegates to a private method — a sign the method exists only for test visibility.
 */
public class TestOnlyDelegateCheck extends AbstractCheck {

    static final String MSG_KEY = "test.only.delegate";

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
        return new int[] {CLASS_DEF, RECORD_DEF};
    }

    @Override
    public void visitToken(DetailAST ast) {
        DetailAST objBlock = ast.findFirstToken(OBJBLOCK);
        if (objBlock == null) {
            return;
        }

        Set<String> privateMethods = collectPrivateMethodNames(objBlock);
        if (privateMethods.isEmpty()) {
            return;
        }

        DetailAST child = objBlock.getFirstChild();
        while (child != null) {
            if (child.getType() == METHOD_DEF && !isPrivate(child) && !AstAnnotationUtil.hasAnnotationNamed(child, "Override")) {
                checkForThinDelegate(child, privateMethods);
            }
            child = child.getNextSibling();
        }
    }

    private void checkForThinDelegate(DetailAST methodDef, Set<String> privateMethods) {
        DetailAST slist = methodDef.findFirstToken(SLIST);
        if (slist == null) {
            return;
        }

        DetailAST firstChild = slist.getFirstChild();
        if (firstChild == null) {
            return;
        }

        DetailAST callExpr = null;
        if (firstChild.getType() == LITERAL_RETURN) {
            callExpr = firstChild.findFirstToken(EXPR);
            if (countStatements(slist) != 1) {
                return;
            }
        } else if (firstChild.getType() == EXPR) {
            callExpr = firstChild;
            if (countStatements(slist) != 1) {
                return;
            }
        }

        if (callExpr == null) {
            return;
        }

        DetailAST methodCall = findTopMethodCall(callExpr);
        if (methodCall == null) {
            return;
        }

        String calledName = extractMethodName(methodCall);
        if (calledName != null && privateMethods.contains(calledName)) {
            DetailAST ident = methodDef.findFirstToken(IDENT);
            log(methodDef.getLineNo(), MSG_KEY, ident != null ? ident.getText() : "?", calledName);
        }
    }

    private static DetailAST findTopMethodCall(DetailAST expr) {
        DetailAST child = expr.getFirstChild();
        if (child == null) {
            return null;
        }
        if (child.getType() == METHOD_CALL) {
            return child;
        }
        return null;
    }

    private static String extractMethodName(DetailAST methodCall) {
        if (methodCall.findFirstToken(DOT) != null) {
            return null;
        }
        DetailAST ident = methodCall.findFirstToken(IDENT);
        return ident != null ? ident.getText() : null;
    }

    private static int countStatements(DetailAST slist) {
        int count = 0;
        DetailAST child = slist.getFirstChild();
        while (child != null) {
            int type = child.getType();
            if (type != RCURLY && type != SEMI) {
                count++;
            }
            child = child.getNextSibling();
        }
        return count;
    }

    private static Set<String> collectPrivateMethodNames(DetailAST objBlock) {
        Set<String> names = new HashSet<>();
        DetailAST child = objBlock.getFirstChild();
        while (child != null) {
            if (child.getType() == METHOD_DEF && isPrivate(child)) {
                DetailAST ident = child.findFirstToken(IDENT);
                if (ident != null) {
                    names.add(ident.getText());
                }
            }
            child = child.getNextSibling();
        }
        return names;
    }

    private static boolean isPrivate(DetailAST methodDef) {
        DetailAST modifiers = methodDef.findFirstToken(MODIFIERS);
        if (modifiers == null) {
            return false;
        }
        DetailAST mod = modifiers.getFirstChild();
        while (mod != null) {
            if (mod.getType() == LITERAL_PRIVATE) {
                return true;
            }
            mod = mod.getNextSibling();
        }
        return false;
    }

}
