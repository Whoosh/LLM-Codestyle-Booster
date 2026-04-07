package io.github.llmcodestyle.quality;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import io.github.llmcodestyle.utils.AstUtil;

import static com.puppycrawl.tools.checkstyle.api.TokenTypes.CLASS_DEF;
import static com.puppycrawl.tools.checkstyle.api.TokenTypes.IDENT;
import static com.puppycrawl.tools.checkstyle.api.TokenTypes.PACKAGE_DEF;

/** Enforces that classes named {@code *Util} or {@code *Utils} reside in a package whose last segment is {@code utils}. */
public class UtilClassInUtilsPackageCheck extends AbstractCheck {

    /** Violation message key. */
    static final String MSG_KEY = "util.class.wrong.package";

    private String currentPackage = "";

    @Override
    public int[] getDefaultTokens() {
        return getRequiredTokens();
    }

    @Override
    public int[] getAcceptableTokens() {
        return getRequiredTokens();
    }

    @Override
    public int[] getRequiredTokens() {
        return new int[]{PACKAGE_DEF, CLASS_DEF};
    }

    @Override
    public void beginTree(DetailAST rootAST) {
        currentPackage = "";
    }

    @Override
    public void visitToken(DetailAST ast) {
        if (ast.getType() == PACKAGE_DEF) {
            currentPackage = AstUtil.extractPackageName(ast);
        } else if (ast.getType() == CLASS_DEF) {
            checkClassPlacement(ast);
        }
    }

    private void checkClassPlacement(DetailAST classDef) {
        if (AstUtil.isInnerClass(classDef)) {
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
        if (!"utils".equals(lastSegmentOf(currentPackage))) {
            log(ident.getLineNo(), ident.getColumnNo(), MSG_KEY, name, currentPackage);
        }
    }

    private static String lastSegmentOf(String pkg) {
        if (pkg == null || pkg.isEmpty()) {
            return "";
        }
        int idx = pkg.lastIndexOf('.');
        return idx >= 0 ? pkg.substring(idx + 1) : pkg;
    }

}
