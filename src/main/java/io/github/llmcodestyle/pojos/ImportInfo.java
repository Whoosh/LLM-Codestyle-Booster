package io.github.llmcodestyle.pojos;

import com.puppycrawl.tools.checkstyle.api.DetailAST;

/**
 * Static import descriptor: the AST node, the imported member name, and its declaring class.
 */
public record ImportInfo(DetailAST ast, String memberName, String parentClass) {
}
