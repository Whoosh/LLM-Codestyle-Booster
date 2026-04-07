package io.github.llmcodestyle.layout;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import io.github.llmcodestyle.utils.AstUtil;

import static com.puppycrawl.tools.checkstyle.api.TokenTypes.COMPACT_CTOR_DEF;
import static com.puppycrawl.tools.checkstyle.api.TokenTypes.CTOR_DEF;
import static com.puppycrawl.tools.checkstyle.api.TokenTypes.METHOD_DEF;
import static com.puppycrawl.tools.checkstyle.api.TokenTypes.PARAMETERS;
import static com.puppycrawl.tools.checkstyle.api.TokenTypes.RECORD_COMPONENTS;
import static com.puppycrawl.tools.checkstyle.api.TokenTypes.RECORD_DEF;

/** Flags multi-line parameter lists where a continuation line could be packed onto the previous line. */
public class CompactableParameterListCheck extends AbstractCheck {

    /** Violation message key. */
    static final String MSG_KEY = "compactable.parameter.line";

    private static final int DEFAULT_MAX_LINE_LENGTH = 180;

    private int lineLimit = DEFAULT_MAX_LINE_LENGTH;

    public void setMaxLineLength(int maxLineLength) {
        this.lineLimit = maxLineLength;
    }

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
        return new int[]{METHOD_DEF, CTOR_DEF, COMPACT_CTOR_DEF, RECORD_DEF};
    }

    @Override
    public void visitToken(DetailAST ast) {
        DetailAST params = ast.findFirstToken(PARAMETERS);
        if (params == null && ast.getType() == RECORD_DEF) {
            params = ast.findFirstToken(RECORD_COMPONENTS);
        }
        if (params == null) {
            return;
        }
        int firstLine = params.getLineNo();
        int lastLine = AstUtil.findLastLine(params);
        if (firstLine >= lastLine) {
            return;
        }
        checkConsecutiveLines(firstLine, lastLine);
    }

    private void checkConsecutiveLines(int firstLine, int lastLine) {
        String[] lines = getLines();
        for (int lineIdx = firstLine; lineIdx < lastLine; lineIdx++) {
            String nextStripped = lines[lineIdx].strip();
            if (nextStripped.isEmpty()) {
                continue;
            }
            int currentLen = lines[lineIdx - 1].stripTrailing().length();
            if (currentLen + 1 + nextStripped.length() <= lineLimit) {
                log(lineIdx + 1, 0, MSG_KEY, lineLimit - currentLen, nextStripped.length());
            }
        }
    }

}
