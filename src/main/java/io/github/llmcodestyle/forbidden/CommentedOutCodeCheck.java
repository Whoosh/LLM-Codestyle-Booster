package io.github.llmcodestyle.forbidden;

import com.puppycrawl.tools.checkstyle.api.AbstractFileSetCheck;
import com.puppycrawl.tools.checkstyle.api.FileText;

import java.io.File;
import java.util.regex.Pattern;

/**
 * Flags blocks of commented-out Java code.
 */
public class CommentedOutCodeCheck extends AbstractFileSetCheck {

    /** Violation message key. */
    static final String MSG_KEY = "commented.out.code";

    private static final int DEFAULT_MIN_LINES = 2;

    private static final Pattern CODE_PATTERN = Pattern.compile(
        "^\\s*//"
        + "\\s*("
        + "@\\w+"
        + "|import\\s+"
        + "|\\w+\\s+\\w+\\s*="
        + "|\\w+\\.\\w+\\s*\\("
        + "|\\w+\\s*\\([^)]*\\)\\s*[;{]"
        + "|\\w+\\s+\\w+\\s*\\("
        + "|(public|private|protected|static|final|void|abstract)\\s+"
        + "|if\\s*\\("
        + "|else\\s*(\\{|if)"
        + "|for\\s*\\("
        + "|while\\s*\\("
        + "|return\\s+\\S"
        + "|return;"
        + "|throw\\s+new"
        + "|try\\s*[({]"
        + "|catch\\s*\\("
        + "|\\}\\s*(else|catch|finally)?"
        + "|\\{\\s*$"
        + "|\\w+<[\\w?, ]+>\\s+\\w+"
        + "|assert\\s+\\S"
        + "|new\\s+\\w+"
        + ")"
    );

    private static final Pattern SEPARATOR_PATTERN = Pattern.compile("[-=]{3,}.*");

    private int minConsecutiveLines = DEFAULT_MIN_LINES;

    public void setMinConsecutiveLines(int minConsecutiveLines) {
        this.minConsecutiveLines = minConsecutiveLines;
    }

    @Override
    protected void processFiltered(File file, FileText fileText) {
        int consecutiveStart = -1;
        int consecutiveCount = 0;

        for (int i = 0; i < fileText.size(); i++) {
            if (looksLikeCommentedCode(fileText.get(i))) {
                if (consecutiveCount == 0) {
                    consecutiveStart = i;
                }
                consecutiveCount++;
            } else {
                if (consecutiveCount >= minConsecutiveLines) {
                    log(consecutiveStart + 1, MSG_KEY, consecutiveCount);
                }
                consecutiveCount = 0;
            }
        }
        if (consecutiveCount >= minConsecutiveLines) {
            log(consecutiveStart + 1, MSG_KEY, consecutiveCount);
        }
    }

    private static boolean looksLikeCommentedCode(String line) {
        String stripped = line.strip();
        return stripped.startsWith("//") && !isTextComment(stripped) && CODE_PATTERN.matcher(line).find();
    }

    private static boolean isTextComment(String stripped) {
        String content = stripped.substring(2).strip();
        if (content.isEmpty()) {
            return true;
        }
        if (content.startsWith("TODO") || content.startsWith("FIXME") || content.startsWith("NOTE") || content.startsWith("HACK") || content.startsWith("XXX")) {
            return true;
        }
        return SEPARATOR_PATTERN.matcher(content).matches();
    }
}
