package io.github.llmcodestyle.layout;

import com.puppycrawl.tools.checkstyle.api.AbstractFileSetCheck;
import com.puppycrawl.tools.checkstyle.api.FileText;

import java.io.File;

/**
 * Flags blank lines between a comment and the code that follows it.
 */
public class BlankLineAfterCommentCheck extends AbstractFileSetCheck {

    static final String MSG_KEY = "blank.line.after.comment";

    @Override
    protected void processFiltered(File file, FileText fileText) {
        var scanner = new CommentScanner();
        for (int i = 0; i < fileText.size(); i++) {
            scanner.processLine(fileText.get(i).stripLeading(), i);
        }
    }

    private final class CommentScanner {

        private boolean inBlockComment;
        private int lastCommentLine = -1;
        private boolean sawBlankAfterComment;

        void processLine(String stripped, int lineIdx) {
            if (inBlockComment) {
                handleBlockCommentBody(stripped, lineIdx);
            } else if (stripped.startsWith("//")) {
                markComment(lineIdx);
            } else if (stripped.startsWith("/*")) {
                handleBlockCommentStart(stripped, lineIdx);
            } else if (stripped.isEmpty()) {
                if (lastCommentLine >= 0) {
                    sawBlankAfterComment = true;
                }
            } else {
                flushAndReset();
            }
        }

        private void handleBlockCommentBody(String stripped, int lineIdx) {
            if (!stripped.contains("*/")) {
                return;
            }
            inBlockComment = false;
            if (stripped.substring(stripped.indexOf("*/") + 2).strip().isEmpty()) {
                markComment(lineIdx);
            } else {
                flushAndReset();
            }
        }

        private void handleBlockCommentStart(String stripped, int lineIdx) {
            if (stripped.contains("*/")) {
                markComment(lineIdx);
            } else {
                inBlockComment = true;
            }
        }

        private void markComment(int lineIdx) {
            lastCommentLine = lineIdx;
            sawBlankAfterComment = false;
        }

        private void flushAndReset() {
            reportIfNeeded(sawBlankAfterComment, lastCommentLine);
            lastCommentLine = -1;
            sawBlankAfterComment = false;
        }
    }

    private void reportIfNeeded(boolean sawBlank, int commentLine) {
        if (sawBlank && commentLine >= 0) {
            log(commentLine + 1, MSG_KEY);
        }
    }
}
