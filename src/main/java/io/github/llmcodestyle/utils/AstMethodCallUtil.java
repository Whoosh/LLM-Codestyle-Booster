package io.github.llmcodestyle.utils;

import com.puppycrawl.tools.checkstyle.api.DetailAST;

import static com.puppycrawl.tools.checkstyle.api.TokenTypes.*;
import static io.github.llmcodestyle.utils.AstQueryUtil.*;

/**
 * Shared utilities for analysing {@code METHOD_CALL} AST nodes:
 * method name extraction, receiver resolution, argument access, and chain counting.
 */
public final class AstMethodCallUtil {

    private AstMethodCallUtil() {
    }

    /**
     * Extracts the method name from a {@code METHOD_CALL} node. Returns empty string if not resolvable.
     * Handles both {@code foo()} (IDENT child) and {@code obj.foo()} (DOT with last child IDENT).
     */
    public static String extractMethodName(DetailAST methodCall) {
        DetailAST dot = methodCall.findFirstToken(DOT);
        if (dot != null) {
            DetailAST last = dot.getLastChild();
            return last != null && last.getType() == IDENT ? last.getText() : "";
        }
        DetailAST ident = methodCall.findFirstToken(IDENT);
        return ident != null ? ident.getText() : "";
    }

    /**
     * Extracts the receiver name from a {@code METHOD_CALL} node (the object before the dot).
     * Returns empty string if the call has no dot receiver or receiver is not a simple IDENT.
     */
    public static String extractReceiverName(DetailAST methodCall) {
        DetailAST dot = methodCall.findFirstToken(DOT);
        if (dot == null) {
            return "";
        }
        DetailAST receiver = dot.getFirstChild();
        return receiver != null && receiver.getType() == IDENT ? receiver.getText() : "";
    }

    /**
     * Extracts the text of the first argument from a {@code METHOD_CALL} node's ELIST.
     * Returns empty string if no arguments or first arg is not a simple IDENT.
     */
    public static String extractFirstArgText(DetailAST methodCall) {
        return findFirstTextInChain(methodCall, ELIST, EXPR, IDENT);
    }

    /**
     * Counts the number of chained method calls rooted at {@code methodCall}
     * (e.g. {@code a.b().c().d()} has chain length 3).
     */
    public static int countMethodChain(DetailAST methodCall) {
        int count = 1;
        DetailAST dot = methodCall.findFirstToken(DOT);
        while (dot != null) {
            DetailAST child = dot.getFirstChild();
            if (child != null && child.getType() == METHOD_CALL) {
                count++;
                dot = child.findFirstToken(DOT);
            } else {
                break;
            }
        }
        return count;
    }
}
