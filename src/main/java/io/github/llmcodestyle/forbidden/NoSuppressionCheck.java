package io.github.llmcodestyle.forbidden;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;

import static com.puppycrawl.tools.checkstyle.api.TokenTypes.*;

/**
 * Forbids suppression annotations and inline suppression comments. Catches {@code @SuppressWarnings}, {@code @SuppressFBWarnings}, and NOPMD/CHECKSTYLE:OFF comments.
 */
public class NoSuppressionCheck extends AbstractCheck {

    /**
     * Violation message key for annotation suppressions.
     */
    static final String MSG_ANNOTATION = "no.suppression.annotation";

    /**
     * Violation message key for comment suppressions.
     */
    static final String MSG_COMMENT = "no.suppression.comment";

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
        return new int[] {ANNOTATION};
    }

    @Override
    public void beginTree(DetailAST rootAST) {
        String[] lines = getLines();
        for (int i = 0; i < lines.length; i++) {
            if (hasSuppressComment(lines[i])) {
                log(i + 1, 0, MSG_COMMENT, lines[i].trim());
            }
        }
    }

    @Override
    public void visitToken(DetailAST ast) {
        DetailAST ident = ast.findFirstToken(IDENT);
        if (ident == null) {
            return;
        }
        String name = ident.getText();
        if ("SuppressWarnings".equals(name) || "SuppressFBWarnings".equals(name)) {
            log(ast.getLineNo(), ast.getColumnNo(), MSG_ANNOTATION, name);
        }
    }

    private static boolean hasSuppressComment(String line) {
        int commentStart = findCommentStart(line);
        if (commentStart < 0) {
            return false;
        }
        String commentPart = line.substring(commentStart).toUpperCase(java.util.Locale.ROOT);
        return commentPart.contains("NOPMD") || commentPart.contains("CHECKSTYLE:OFF") || commentPart.contains("SUPPRESSFBWARNINGS");
    }

    private static int findCommentStart(String line) {
        boolean inString = false;
        boolean inChar = false;
        boolean escape = false;
        for (int i = 0; i < line.length() - 1; i++) {
            char c = line.charAt(i);
            if (escape) {
                escape = false;
                continue;
            }
            if ((inString || inChar) && c == '\\') {
                escape = true;
                continue;
            }
            if (!inChar && c == '"') {
                inString = !inString;
                continue;
            }
            if (!inString && c == '\'') {
                inChar = !inChar;
                continue;
            }
            if (!inString && !inChar && c == '/' && line.charAt(i + 1) == '/') {
                return i;
            }
        }
        return -1;
    }
}
