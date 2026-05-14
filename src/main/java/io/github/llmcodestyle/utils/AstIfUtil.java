package io.github.llmcodestyle.utils;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import jakarta.annotation.Nullable;

import static com.puppycrawl.tools.checkstyle.api.TokenTypes.*;

/**
 * Shared helpers for navigating {@code LITERAL_IF} AST nodes.
 */
public final class AstIfUtil {

    private AstIfUtil() {
    }

    /**
     * Returns {@code true} if the {@code if} statement has a direct {@code else} clause.
     * Does not distinguish between {@code else { ... }} and {@code else if (...)}; both
     * are LITERAL_ELSE children.
     */
    public static boolean hasElseClause(DetailAST ifAst) {
        return ifAst.findFirstToken(LITERAL_ELSE) != null;
    }

    /**
     * Returns the body of the {@code then} branch — the AST node immediately following
     * the closing {@code RPAREN} of the condition. May be an {@code SLIST} (block body)
     * or a single statement node ({@code LITERAL_RETURN}, {@code EXPR}, {@code LITERAL_THROW}, etc.)
     * when the body has no braces. Returns {@code null} if the if has no {@code RPAREN} or
     * no statement after it.
     */
    @Nullable
    public static DetailAST extractThenBody(DetailAST ifAst) {
        DetailAST rparen = ifAst.findFirstToken(RPAREN);
        return rparen == null ? null : rparen.getNextSibling();
    }
}
