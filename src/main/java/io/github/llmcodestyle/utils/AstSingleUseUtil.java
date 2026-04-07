package io.github.llmcodestyle.utils;

import com.puppycrawl.tools.checkstyle.api.DetailAST;

import static com.puppycrawl.tools.checkstyle.api.TokenTypes.FOR_EACH_CLAUSE;
import static com.puppycrawl.tools.checkstyle.api.TokenTypes.IDENT;
import static com.puppycrawl.tools.checkstyle.api.TokenTypes.LAMBDA;
import static com.puppycrawl.tools.checkstyle.api.TokenTypes.LCURLY;
import static com.puppycrawl.tools.checkstyle.api.TokenTypes.LITERAL_CATCH;
import static com.puppycrawl.tools.checkstyle.api.TokenTypes.LITERAL_DO;
import static com.puppycrawl.tools.checkstyle.api.TokenTypes.LITERAL_ELSE;
import static com.puppycrawl.tools.checkstyle.api.TokenTypes.LITERAL_FINALLY;
import static com.puppycrawl.tools.checkstyle.api.TokenTypes.LITERAL_FOR;
import static com.puppycrawl.tools.checkstyle.api.TokenTypes.LITERAL_IF;
import static com.puppycrawl.tools.checkstyle.api.TokenTypes.LITERAL_SYNCHRONIZED;
import static com.puppycrawl.tools.checkstyle.api.TokenTypes.LITERAL_TRY;
import static com.puppycrawl.tools.checkstyle.api.TokenTypes.LITERAL_WHILE;
import static com.puppycrawl.tools.checkstyle.api.TokenTypes.RCURLY;
import static com.puppycrawl.tools.checkstyle.api.TokenTypes.SEMI;

import java.util.ArrayList;
import java.util.List;

/** Utility methods shared between single-use local variable checks. */
public final class AstSingleUseUtil {

    private AstSingleUseUtil() {
    }

    /** Collects direct non-punctuation children of an SLIST node into a list. */
    public static List<DetailAST> collectStatements(DetailAST slist) {
        List<DetailAST> result = new ArrayList<>();
        DetailAST child = slist.getFirstChild();
        while (child != null) {
            int type = child.getType();
            if (type != RCURLY && type != LCURLY && type != SEMI) {
                result.add(child);
            }
            child = child.getNextSibling();
        }
        return result;
    }

    /** Counts occurrences of an identifier with {@code name} in the subtree rooted at {@code root}. */
    public static int countIdent(DetailAST root, String name) {
        int count = 0;
        if (root.getType() == IDENT && name.equals(root.getText())) {
            count++;
        }
        DetailAST child = root.getFirstChild();
        while (child != null) {
            count += countIdent(child, name);
            child = child.getNextSibling();
        }
        return count;
    }

    /** Returns {@code true} if {@code varName} appears within a loop or lambda body inside {@code statement}. */
    public static boolean isInsideRepeatingContext(DetailAST statement, String varName) {
        return containsIdentInRepeatingContext(statement, varName, false);
    }

    /** Recursive helper for {@link #isInsideRepeatingContext}. */
    public static boolean containsIdentInRepeatingContext(DetailAST node, String varName, boolean inRepeat) {
        if (node.getType() == IDENT && varName.equals(node.getText()) && inRepeat) {
            return true;
        }
        DetailAST child = node.getFirstChild();
        while (child != null) {
            if (containsIdentInRepeatingContext(child, varName, childRepeatStatus(node, child, inRepeat))) {
                return true;
            }
            child = child.getNextSibling();
        }
        return false;
    }

    /** Determines whether {@code child} should be considered inside a repeating context. For-each iterable is not repeating; loop bodies and lambdas are. */
    public static boolean childRepeatStatus(DetailAST parent, DetailAST child, boolean parentRepeat) {
        int parentType = parent.getType();
        if (parentType == LITERAL_FOR) {
            return child.getType() != FOR_EACH_CLAUSE || parentRepeat;
        }
        if (parentType == LITERAL_WHILE || parentType == LITERAL_DO || parentType == LAMBDA) {
            return true;
        }
        return parentRepeat;
    }

    /** Returns {@code true} if {@code type} is a loop or conditional control-flow token. */
    public static boolean isLoopOrCondition(int type) {
        return type == LITERAL_WHILE || type == LITERAL_FOR || type == LITERAL_DO || type == LITERAL_IF || type == LITERAL_ELSE;
    }

    /** Returns {@code true} if {@code type} is an exception-handling control-flow token. */
    public static boolean isExceptionBlock(int type) {
        return type == LITERAL_TRY || type == LITERAL_CATCH || type == LITERAL_FINALLY || type == LITERAL_SYNCHRONIZED;
    }
}
