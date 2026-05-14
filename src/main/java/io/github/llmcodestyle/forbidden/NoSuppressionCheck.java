package io.github.llmcodestyle.forbidden;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;

import static com.puppycrawl.tools.checkstyle.api.TokenTypes.*;

/**
 * Forbids suppression annotations and inline suppression comments.
 * Catches {@code @SuppressWarnings} (and its SpotBugs equivalent) plus inline directives in comments.
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
    private static final int[] TOKENS = {ANNOTATION};

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
    public void beginTree(DetailAST rootAST) {
        String[] lines = getLines();
        boolean inBlock = false;
        for (int i = 0; i < lines.length; i++) {
            inBlock = inBlock ? processInsideBlock(lines[i], i) : processOutsideBlock(lines[i], i);
        }
    }

    private boolean processOutsideBlock(String line, int idx) {
        if (hasSuppressComment(line)) {
            log(idx + 1, 0, MSG_COMMENT, line.trim());
        }
        return startsUnclosedBlock(line);
    }

    private boolean processInsideBlock(String line, int idx) {
        if (containsSuppressionToken(line)) {
            log(idx + 1, 0, MSG_COMMENT, line.trim());
        }
        int blockEnd = line.indexOf("*/");
        if (blockEnd < 0) {
            return true;
        }
        String afterBlock = line.substring(blockEnd + 2);
        if (hasSuppressComment(afterBlock)) {
            log(idx + 1, 0, MSG_COMMENT, line.trim());
            return false;
        }
        int reopen = afterBlock.indexOf("/*");
        return reopen >= 0 && afterBlock.indexOf("*/", reopen + 2) < 0;
    }

    private static boolean startsUnclosedBlock(String line) {
        int blockOpen = findBlockCommentStart(line);
        return blockOpen >= 0 && line.indexOf("*/", blockOpen + 2) < 0;
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
        return hasSuppressInLineComment(line) || hasSuppressInBlockComment(line);
    }

    private static boolean hasSuppressInLineComment(String line) {
        int lineCommentStart = findCommentStart(line);
        return lineCommentStart >= 0 && containsSuppressionToken(line.substring(lineCommentStart));
    }

    private static boolean hasSuppressInBlockComment(String line) {
        int blockStart = findBlockCommentStart(line);
        if (blockStart < 0) {
            return false;
        }
        int blockEnd = line.indexOf("*/", blockStart + 2);
        if (containsSuppressionToken(blockEnd >= 0 ? line.substring(blockStart, blockEnd + 2) : line.substring(blockStart))) {
            return true;
        }
        if (blockEnd < 0) {
            return false;
        }
        // A block comment may close mid-line and a // line-comment may follow with a token.
        String tail = line.substring(blockEnd + 2);
        int tailLineComment = findCommentStart(tail);
        return tailLineComment >= 0 && containsSuppressionToken(tail.substring(tailLineComment));
    }

    private static boolean containsSuppressionToken(String text) {
        String upper = text.toUpperCase(java.util.Locale.ROOT);
        return upper.contains("NOPMD") || upper.contains("CHECKSTYLE:OFF") || upper.contains("SUPPRESSFBWARNINGS");
    }

    private static int findCommentStart(String line) {
        return scanForComment(line, '/');
    }

    private static int findBlockCommentStart(String line) {
        return scanForComment(line, '*');
    }

    private static int scanForComment(String line, char secondChar) {
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
            if (!inString && !inChar && c == '/' && line.charAt(i + 1) == secondChar) {
                return i;
            }
        }
        return -1;
    }
}
