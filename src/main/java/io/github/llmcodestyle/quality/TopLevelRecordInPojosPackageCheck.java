package io.github.llmcodestyle.quality;

import com.puppycrawl.tools.checkstyle.api.DetailAST;

import static com.puppycrawl.tools.checkstyle.api.TokenTypes.*;
import static io.github.llmcodestyle.utils.AstUtil.*;

/**
 * Enforces that top-level {@code record} declarations live in a package whose last segment
 * equals the configured {@code packageSuffix} (default {@code pojos}). Pure data carriers
 * should be grouped in a dedicated package for reuse and discoverability.
 *
 * <p>Nested records are out of scope — see {@link UnrelatedNestedRecordCheck} which applies
 * a separate heuristic to records declared inside another type.
 */
public class TopLevelRecordInPojosPackageCheck extends PackageScopedCheckBase {

    /**
     * Violation message key.
     */
    static final String MSG_KEY = "top.level.record.wrong.package";
    private static final int[] DOMAIN_TOKENS = {RECORD_DEF};
    private static final String DEFAULT_PACKAGE_SUFFIX = "pojos";

    private String packageSuffix = DEFAULT_PACKAGE_SUFFIX;

    public void setPackageSuffix(String packageSuffix) {
        this.packageSuffix = packageSuffix;
    }

    @Override
    protected int[] domainTokens() {
        return DOMAIN_TOKENS.clone();
    }

    @Override
    protected void visitDomainToken(DetailAST recordDef) {
        if (isNestedType(recordDef) || packageSuffix.equals(lastPackageSegment(currentPackage()))) {
            return;
        }
        DetailAST ident = recordDef.findFirstToken(IDENT);
        if (ident != null) {
            log(ident.getLineNo(), ident.getColumnNo(), MSG_KEY, ident.getText(), currentPackage(), packageSuffix);
        }
    }
}
