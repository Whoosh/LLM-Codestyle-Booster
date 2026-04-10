package io.github.llmcodestyle.utils;

import com.puppycrawl.tools.checkstyle.api.DetailAST;

import static com.puppycrawl.tools.checkstyle.api.TokenTypes.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Utility methods shared between single-use local variable checks.
 */
public final class AstSingleUseUtil {

    private static final Set<Integer> REPEAT_CONTEXT_PARENTS = Set.of(LITERAL_WHILE, LITERAL_DO, LAMBDA);
    private static final Set<Integer> LOOP_OR_CONDITION_TOKENS = Set.of(LITERAL_WHILE, LITERAL_FOR, LITERAL_DO, LITERAL_IF, LITERAL_ELSE);
    private static final Set<Integer> EXCEPTION_BLOCK_TOKENS = Set.of(LITERAL_TRY, LITERAL_CATCH, LITERAL_FINALLY, LITERAL_SYNCHRONIZED);

    private AstSingleUseUtil() {
    }

    /**
     * Collects direct non-punctuation children of an SLIST node into a list.
     */
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

    /**
     * Counts occurrences of an identifier with {@code name} in the subtree rooted at {@code root}.
     */
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

    /**
     * Returns {@code true} if {@code varName} appears within a loop or lambda body inside {@code statement}.
     */
    public static boolean isInsideRepeatingContext(DetailAST statement, String varName) {
        return containsIdentInRepeatingContext(statement, varName, false);
    }

    /**
     * Recursive helper for {@link #isInsideRepeatingContext}.
     */
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

    /**
     * Determines whether {@code child} should be considered inside a repeating context. For-each iterable is not repeating; loop bodies and lambdas are.
     */
    public static boolean childRepeatStatus(DetailAST parent, DetailAST child, boolean parentRepeat) {
        int parentType = parent.getType();
        if (parentType == LITERAL_FOR) {
            return child.getType() != FOR_EACH_CLAUSE || parentRepeat;
        }
        return REPEAT_CONTEXT_PARENTS.contains(parentType) || parentRepeat;
    }

    /**
     * Returns {@code true} if {@code type} is a loop or conditional control-flow token.
     */
    public static boolean isLoopOrCondition(int type) {
        return LOOP_OR_CONDITION_TOKENS.contains(type);
    }

    /**
     * Returns {@code true} if {@code type} is an exception-handling control-flow token.
     */
    public static boolean isExceptionBlock(int type) {
        return EXCEPTION_BLOCK_TOKENS.contains(type);
    }
}
