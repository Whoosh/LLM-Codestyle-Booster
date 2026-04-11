package io.github.llmcodestyle.quality;

import com.puppycrawl.tools.checkstyle.api.DetailAST;

import static com.puppycrawl.tools.checkstyle.api.TokenTypes.*;
import static io.github.llmcodestyle.utils.AstUtil.*;

/**
 * Enforces that classes named {@code *Util} or {@code *Utils} reside in a package whose last segment is {@code utils}.
 */
public class UtilClassInUtilsPackageCheck extends PackageScopedCheckBase {

    /**
     * Violation message key.
     */
    static final String MSG_KEY = "util.class.wrong.package";
    private static final int[] DOMAIN_TOKENS = {CLASS_DEF};

    @Override
    protected int[] domainTokens() {
        return DOMAIN_TOKENS.clone();
    }

    @Override
    protected void visitDomainToken(DetailAST classDef) {
        if (isInnerClass(classDef)) {
            return;
        }
        DetailAST ident = classDef.findFirstToken(IDENT);
        if (ident == null) {
            return;
        }
        String name = ident.getText();
        if (!name.endsWith("Util") && !name.endsWith("Utils")) {
            return;
        }
        if (!"utils".equals(lastPackageSegment(currentPackage()))) {
            log(ident.getLineNo(), ident.getColumnNo(), MSG_KEY, name, currentPackage());
        }
    }
}
