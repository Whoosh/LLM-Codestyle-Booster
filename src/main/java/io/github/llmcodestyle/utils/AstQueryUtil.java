package io.github.llmcodestyle.utils;

import com.puppycrawl.tools.checkstyle.api.DetailAST;

import static com.puppycrawl.tools.checkstyle.api.TokenTypes.*;

import java.util.Set;

/**
 * Shared AST query helpers: child lookups, identifier extraction, and token-chain walks.
 */
public final class AstQueryUtil {

    private AstQueryUtil() {
    }

    /**
     * Returns {@code true} if an {@code if} statement is the {@code else-if} branch of an
     * enclosing conditional (its direct parent is {@code LITERAL_ELSE}).
     */
    public static boolean isElseIf(DetailAST ifAst) {
        DetailAST parent = ifAst.getParent();
        return parent != null && parent.getType() == LITERAL_ELSE;
    }

    /**
     * Extracts the IDENT child of a definition node and adds its text to {@code names}.
     * No-op if the node has no {@code IDENT} child.
     */
    public static void addIdentTo(DetailAST def, Set<String> names) {
        DetailAST ident = def.findFirstToken(IDENT);
        if (ident != null) {
            names.add(ident.getText());
        }
    }

    /**
     * Walks a chain of {@code findFirstToken} calls starting from {@code start},
     * returning the text of the terminal node. Returns an empty string if any
     * intermediate lookup returns {@code null}, short-circuiting safely.
     *
     * <p>Example: {@code findFirstTextInChain(methodCall, ELIST, EXPR, IDENT)}
     * is equivalent to
     * {@code methodCall.findFirstToken(ELIST).findFirstToken(EXPR).findFirstToken(IDENT).getText()}
     * with null checks at every step.
     */
    public static String findFirstTextInChain(DetailAST start, int... tokenChain) {
        DetailAST node = start;
        for (int tokenType : tokenChain) {
            if (node == null) {
                return "";
            }
            node = node.findFirstToken(tokenType);
        }
        return node != null ? node.getText() : "";
    }
}
