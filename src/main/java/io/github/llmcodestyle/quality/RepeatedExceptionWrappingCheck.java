package io.github.llmcodestyle.quality;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import io.github.llmcodestyle.utils.AstQueryUtil;
import io.github.llmcodestyle.utils.AstUtil;

import static com.puppycrawl.tools.checkstyle.api.TokenTypes.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Detects 3+ catch blocks in a class with identical catch-and-rethrow-wrapped pattern.
 * Suggests extracting into a utility method like {@code wrapChecked(Callable)}.
 */
public class RepeatedExceptionWrappingCheck extends AbstractCheck {

    /**
     * Violation message key.
     */
    static final String MSG_KEY = "repeated.exception.wrapping";
    private static final int[] TOKENS = {CLASS_DEF};

    private static final int DEFAULT_MIN_OCCURRENCES = 3;

    /**
     * Minimum number of identical wrapping patterns to trigger a violation.
     */
    private int minOccurrences = DEFAULT_MIN_OCCURRENCES;

    private final Map<String, List<DetailAST>> wrappingPatterns = new HashMap<>();

    /**
     * Set the minimum number of identical wrapping patterns.
     *
     * @param minOccurrences the threshold
     */
    public void setMinOccurrences(int minOccurrences) {
        this.minOccurrences = minOccurrences;
    }

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
    public void visitToken(DetailAST classAst) {
        wrappingPatterns.clear();
        collectCatchBlocks(classAst);

        for (List<DetailAST> group : wrappingPatterns.values()) {
            if (group.size() >= minOccurrences) {
                for (DetailAST catchAst : group) {
                    log(catchAst, MSG_KEY, group.size());
                }
            }
        }
        wrappingPatterns.clear();
    }

    private void collectCatchBlocks(DetailAST node) {
        for (DetailAST child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child.getType() == LITERAL_CATCH) {
                analyzeCatch(child);
            }
            if (child.getType() != CLASS_DEF && child.getType() != ENUM_DEF && child.getType() != RECORD_DEF) {
                collectCatchBlocks(child);
            }
        }
    }

    private void analyzeCatch(DetailAST catchAst) {
        DetailAST slist = catchAst.findFirstToken(SLIST);
        if (slist == null || !isSingleThrowNew(slist)) {
            return;
        }
        String caughtType = extractCaughtType(catchAst);
        String thrownType = AstQueryUtil.findFirstTextInChain(slist, LITERAL_THROW, EXPR, LITERAL_NEW, IDENT);
        if (caughtType.isEmpty() || thrownType.isEmpty()) {
            return;
        }
        wrappingPatterns.computeIfAbsent(caughtType + " -> " + thrownType, k -> new ArrayList<>()).add(catchAst);
    }

    private static boolean isSingleThrowNew(DetailAST slist) {
        int stmtCount = 0;
        boolean hasThrow = false;
        for (DetailAST child = slist.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child.getType() == RCURLY || child.getType() == SEMI) {
                continue;
            }
            stmtCount++;
            if (child.getType() == LITERAL_THROW) {
                DetailAST expr = child.findFirstToken(EXPR);
                if (expr != null && expr.findFirstToken(LITERAL_NEW) != null) {
                    hasThrow = true;
                }
            }
        }
        return stmtCount == 1 && hasThrow;
    }

    private static String extractCaughtType(DetailAST catchAst) {
        DetailAST paramDef = catchAst.findFirstToken(PARAMETER_DEF);
        return paramDef != null ? AstUtil.extractTypeName(paramDef) : "";
    }
}
