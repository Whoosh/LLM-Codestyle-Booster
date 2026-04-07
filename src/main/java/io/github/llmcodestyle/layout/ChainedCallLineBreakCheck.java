package io.github.llmcodestyle.layout;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import io.github.llmcodestyle.utils.AstUtil;
import io.github.llmcodestyle.utils.AstMethodCallUtil;

import static com.puppycrawl.tools.checkstyle.api.TokenTypes.*;

/**
 * Flags chained method calls (4+ dots) that are all on the same line.
 */
public class ChainedCallLineBreakCheck extends AbstractCheck {

    private static final String MSG_KEY = "chained.call.line.break";
    private static final int DEFAULT_MIN_CHAIN_LENGTH = 4;
    private static final int[] REQUIRED_TOKENS = {METHOD_CALL};

    private int minChainLength = DEFAULT_MIN_CHAIN_LENGTH;

    public void setMinChainLength(int minChainLength) {
        this.minChainLength = minChainLength;
    }

    @Override
    public int[] getDefaultTokens() {
        return REQUIRED_TOKENS.clone();
    }

    @Override
    public int[] getAcceptableTokens() {
        return REQUIRED_TOKENS.clone();
    }

    @Override
    public int[] getRequiredTokens() {
        return REQUIRED_TOKENS.clone();
    }

    @Override
    public void visitToken(DetailAST ast) {
        if (isPartOfOuterChain(ast)) {
            return;
        }

        int chainLength = AstMethodCallUtil.countMethodChain(ast);
        if (chainLength < minChainLength) {
            return;
        }

        int firstLine = findFirstLine(ast);
        if (firstLine == AstUtil.findLastLine(ast)) {
            log(firstLine, MSG_KEY, chainLength);
        }
    }

    private static boolean isPartOfOuterChain(DetailAST methodCall) {
        DetailAST parent = methodCall.getParent();
        if (parent != null && parent.getType() == DOT) {
            DetailAST grandParent = parent.getParent();
            return grandParent != null && grandParent.getType() == METHOD_CALL;
        }
        return false;
    }

    private static int findFirstLine(DetailAST ast) {
        int first = ast.getLineNo();
        DetailAST child = ast.getFirstChild();
        while (child != null) {
            int childFirst = findFirstLine(child);
            if (childFirst < first) {
                first = childFirst;
            }
            child = child.getNextSibling();
        }
        return first;
    }

}
