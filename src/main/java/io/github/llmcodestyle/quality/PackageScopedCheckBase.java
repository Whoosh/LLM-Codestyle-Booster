package io.github.llmcodestyle.quality;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;

import static com.puppycrawl.tools.checkstyle.api.TokenTypes.*;
import static io.github.llmcodestyle.utils.AstUtil.*;

/**
 * Base class for checks that need to know the enclosing {@code package} of each top-level
 * type declaration. Subclasses declare their domain tokens via {@link #domainTokens()};
 * {@code PACKAGE_DEF} is added automatically and handled by this class. The current package
 * name is exposed via {@link #currentPackage()}.
 */
abstract class PackageScopedCheckBase extends AbstractCheck {

    private String pkgName = "";

    /**
     * Token types this subclass wants to receive via {@link #visitDomainToken(DetailAST)}.
     * {@code PACKAGE_DEF} is added on top of these automatically.
     */
    protected abstract int[] domainTokens();

    /**
     * Called for every token in {@link #domainTokens()}. By the time this fires,
     * {@link #currentPackage()} reflects the current compilation unit's package.
     */
    protected abstract void visitDomainToken(DetailAST ast);

    protected final String currentPackage() {
        return pkgName;
    }

    @Override
    public final int[] getDefaultTokens() {
        return buildTokens();
    }

    @Override
    public final int[] getAcceptableTokens() {
        return buildTokens();
    }

    @Override
    public final int[] getRequiredTokens() {
        return buildTokens();
    }

    @Override
    public final void beginTree(DetailAST rootAST) {
        pkgName = "";
    }

    @Override
    public final void visitToken(DetailAST ast) {
        if (ast.getType() == PACKAGE_DEF) {
            pkgName = extractPackageName(ast);
        } else {
            visitDomainToken(ast);
        }
    }

    private int[] buildTokens() {
        int[] extra = domainTokens();
        int[] all = new int[extra.length + 1];
        all[0] = PACKAGE_DEF;
        System.arraycopy(extra, 0, all, 1, extra.length);
        return all;
    }
}
