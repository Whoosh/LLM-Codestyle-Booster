package io.github.llmcodestyle.simplify;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;

import static com.puppycrawl.tools.checkstyle.api.TokenTypes.*;

/**
 * Detects {@code if (map.containsKey(key)) ... map.get(key)} pattern.
 * Suggests using {@code getOrDefault}, {@code computeIfAbsent}, or a single {@code get} with null check.
 */
public class MapContainsKeyThenGetCheck extends AbstractCheck {

    /**
     * Violation message key.
     */
    static final String MSG_KEY = "map.contains.then.get";

    @Override
    public int[] getDefaultTokens() {
        return new int[] {LITERAL_IF};
    }

    @Override
    public int[] getAcceptableTokens() {
        return new int[] {LITERAL_IF};
    }

    @Override
    public int[] getRequiredTokens() {
        return new int[] {LITERAL_IF};
    }

    @Override
    public void visitToken(DetailAST ifAst) {
        DetailAST expr = ifAst.findFirstToken(EXPR);
        if (expr == null) {
            return;
        }
        DetailAST containsCall = findContainsKeyCall(expr);
        if (containsCall == null) {
            return;
        }
        String receiver = extractReceiverText(containsCall);
        String keyArg = extractFirstArgText(containsCall);
        if (receiver.isEmpty() || keyArg.isEmpty()) {
            return;
        }
        if (containsGetCallOnSameReceiver(ifAst, receiver, keyArg)) {
            log(ifAst, MSG_KEY);
        }
    }

    private static DetailAST findContainsKeyCall(DetailAST node) {
        if (node.getType() == METHOD_CALL && isContainsKeyCall(node)) {
            return node;
        }
        for (DetailAST child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
            DetailAST found = findContainsKeyCall(child);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    private static boolean isContainsKeyCall(DetailAST methodCall) {
        DetailAST dot = methodCall.findFirstToken(DOT);
        if (dot == null) {
            return false;
        }
        DetailAST methodIdent = dot.getLastChild();
        return methodIdent != null && "containsKey".equals(methodIdent.getText());
    }

    private static boolean containsGetCallOnSameReceiver(DetailAST ifAst, String receiver, String keyArg) {
        DetailAST slist = ifAst.findFirstToken(SLIST);
        if (slist != null && hasGetCall(slist, receiver, keyArg)) {
            return true;
        }
        DetailAST elseAst = ifAst.findFirstToken(LITERAL_ELSE);
        return elseAst != null && hasGetCall(elseAst, receiver, keyArg);
    }

    private static boolean hasGetCall(DetailAST node, String receiver, String keyArg) {
        if (node.getType() == METHOD_CALL && isGetCallOnReceiver(node, receiver, keyArg)) {
            return true;
        }
        for (DetailAST child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (hasGetCall(child, receiver, keyArg)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isGetCallOnReceiver(DetailAST methodCall, String receiver, String keyArg) {
        DetailAST dot = methodCall.findFirstToken(DOT);
        if (dot == null) {
            return false;
        }
        DetailAST methodIdent = dot.getLastChild();
        if (methodIdent == null || !"get".equals(methodIdent.getText())) {
            return false;
        }
        return receiver.equals(extractReceiverText(methodCall)) && keyArg.equals(extractFirstArgText(methodCall));
    }

    private static String extractReceiverText(DetailAST methodCall) {
        DetailAST dot = methodCall.findFirstToken(DOT);
        if (dot == null) {
            return "";
        }
        DetailAST receiver = dot.getFirstChild();
        if (receiver == null) {
            return "";
        }
        return receiver.getType() == IDENT ? receiver.getText() : "";
    }

    private static String extractFirstArgText(DetailAST methodCall) {
        DetailAST elist = methodCall.findFirstToken(ELIST);
        if (elist == null) {
            return "";
        }
        DetailAST firstExpr = elist.findFirstToken(EXPR);
        if (firstExpr == null) {
            return "";
        }
        DetailAST ident = firstExpr.findFirstToken(IDENT);
        return ident != null ? ident.getText() : "";
    }
}
