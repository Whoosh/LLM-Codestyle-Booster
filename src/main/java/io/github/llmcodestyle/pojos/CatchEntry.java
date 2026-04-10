package io.github.llmcodestyle.pojos;

import com.puppycrawl.tools.checkstyle.api.DetailAST;

/**
 * Catch clause descriptor: the AST node and a body fingerprint used for duplicate detection.
 */
public record CatchEntry(DetailAST ast, String fingerprint) {
}
