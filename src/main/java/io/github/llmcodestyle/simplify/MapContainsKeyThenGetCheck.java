package io.github.llmcodestyle.simplify;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import io.github.llmcodestyle.utils.AstMethodCallUtil;

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
    private static final int[] TOKENS = {LITERAL_IF};

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
    public void visitToken(DetailAST ifAst) {
        DetailAST expr = ifAst.findFirstToken(EXPR);
        if (expr == null) {
            return;
        }
        DetailAST containsCall = findContainsKeyCall(expr);
        if (containsCall == null) {
            return;
        }
        String receiver = AstMethodCallUtil.extractReceiverName(containsCall);
        String keyArg = AstMethodCallUtil.extractFirstArgText(containsCall);
        if (receiver.isEmpty() || keyArg.isEmpty()) {
            return;
        }
        if (containsGetCallOnSameReceiver(ifAst, receiver, keyArg)) {
            log(ifAst, MSG_KEY);
        }
    }

    private static DetailAST findContainsKeyCall(DetailAST node) {
        if (node.getType() == METHOD_CALL && "containsKey".equals(AstMethodCallUtil.extractMethodName(node))) {
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
        if (!"get".equals(AstMethodCallUtil.extractMethodName(methodCall))) {
            return false;
        }
        return receiver.equals(AstMethodCallUtil.extractReceiverName(methodCall)) && keyArg.equals(AstMethodCallUtil.extractFirstArgText(methodCall));
    }
}
