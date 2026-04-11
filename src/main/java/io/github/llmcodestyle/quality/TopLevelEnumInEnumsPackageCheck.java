package io.github.llmcodestyle.quality;

import com.puppycrawl.tools.checkstyle.api.DetailAST;

import static com.puppycrawl.tools.checkstyle.api.TokenTypes.*;
import static io.github.llmcodestyle.utils.AstUtil.*;

/**
 * Enforces that top-level {@code enum} declarations live in a package whose last segment
 * equals the configured {@code packageSuffix} (default {@code enums}). Freestanding value
 * sets should be grouped in a dedicated package for reuse and discoverability.
 *
 * <p>Nested enums are out of scope — see {@link UnrelatedNestedEnumCheck} which applies a
 * separate heuristic to enums declared inside another type.
 */
public class TopLevelEnumInEnumsPackageCheck extends PackageScopedCheckBase {

    /**
     * Violation message key.
     */
    static final String MSG_KEY = "top.level.enum.wrong.package";
    private static final int[] DOMAIN_TOKENS = {ENUM_DEF};
    private static final String DEFAULT_PACKAGE_SUFFIX = "enums";

    private String packageSuffix = DEFAULT_PACKAGE_SUFFIX;

    public void setPackageSuffix(String packageSuffix) {
        this.packageSuffix = packageSuffix;
    }

    @Override
    protected int[] domainTokens() {
        return DOMAIN_TOKENS.clone();
    }

    @Override
    protected void visitDomainToken(DetailAST enumDef) {
        if (isNestedType(enumDef) || packageSuffix.equals(lastPackageSegment(currentPackage()))) {
            return;
        }
        DetailAST ident = enumDef.findFirstToken(IDENT);
        if (ident != null) {
            log(ident.getLineNo(), ident.getColumnNo(), MSG_KEY, ident.getText(), currentPackage(), packageSuffix);
        }
    }
}
