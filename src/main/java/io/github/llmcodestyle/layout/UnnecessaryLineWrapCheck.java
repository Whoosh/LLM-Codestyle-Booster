package io.github.llmcodestyle.layout;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import io.github.llmcodestyle.utils.AstUtil;

import static com.puppycrawl.tools.checkstyle.api.TokenTypes.*;

/**
 * Flags statements split across multiple lines that would fit on a single line within the max length.
 */
public class UnnecessaryLineWrapCheck extends AbstractCheck {

    private static final String MSG_KEY = "unnecessary.line.wrap";
    private static final int[] TOKENS = {
        METHOD_CALL, METHOD_DEF, CTOR_DEF, COMPACT_CTOR_DEF, VARIABLE_DEF, RECORD_DEF,
        LITERAL_IF, RESOURCE, LITERAL_THROW, LITERAL_RETURN,
        CLASS_DEF, INTERFACE_DEF, ENUM_DEF, LITERAL_TRY,
    };
    private static final int DEFAULT_MAX_LINE_LENGTH = 180;
    private static final int CHAIN_THRESHOLD = 4;

    private int maxLineLength = DEFAULT_MAX_LINE_LENGTH;

    public void setMaxLineLength(int maxLineLength) {
        this.maxLineLength = maxLineLength;
    }

    @Override
    public int[] getDefaultTokens() {
        return TOKENS.clone();
    }

    @Override
    public int[] getAcceptableTokens() {
        return TOKENS.clone();
    }

    @Override
    public int[] getRequiredTokens() {
        return TOKENS.clone();
    }

    @Override
    public void visitToken(DetailAST ast) {
        if (isNestedInCheckedParent(ast)) {
            return;
        }

        if (ast.getType() == LITERAL_TRY && ast.findFirstToken(RESOURCE_SPECIFICATION) == null) {
            return;
        }

        if (ast.getType() == RESOURCE && tryHeaderFitsOnOneLine(ast)) {
            return;
        }

        if (containsLongChainInScope(ast)) {
            return;
        }

        int firstLine = computeFirstLine(ast);
        int lastLine = computeLastLine(ast);

        if (firstLine >= lastLine) {
            return;
        }

        String combined = buildCombinedLine(firstLine, lastLine);
        if (combined.length() <= maxLineLength) {
            log(firstLine, MSG_KEY, combined.length(), maxLineLength);
        }
    }

    private int computeFirstLine(DetailAST ast) {
        int type = ast.getType();
        if (type == METHOD_DEF || type == CTOR_DEF || type == COMPACT_CTOR_DEF || type == RECORD_DEF || type == VARIABLE_DEF || type == RESOURCE) {
            DetailAST typeToken = ast.findFirstToken(TYPE);
            if (typeToken != null) {
                return typeToken.getLineNo();
            }
            DetailAST ident = ast.findFirstToken(IDENT);
            if (ident != null) {
                return ident.getLineNo();
            }
        }
        if (type == CLASS_DEF || type == INTERFACE_DEF || type == ENUM_DEF) {
            return firstNonAnnotationLine(ast);
        }
        return ast.getLineNo();
    }

    private static int firstNonAnnotationLine(DetailAST def) {
        DetailAST modifiers = def.findFirstToken(MODIFIERS);
        if (modifiers != null) {
            DetailAST mod = modifiers.getFirstChild();
            while (mod != null) {
                if (mod.getType() != ANNOTATION) {
                    return mod.getLineNo();
                }
                mod = mod.getNextSibling();
            }
        }
        DetailAST ident = def.findFirstToken(IDENT);
        return ident != null ? ident.getLineNo() : def.getLineNo();
    }

    private int computeLastLine(DetailAST ast) {
        int type = ast.getType();

        if (type == METHOD_DEF || type == CTOR_DEF || type == COMPACT_CTOR_DEF) {
            return findSignatureLastLine(ast);
        }

        if (type == LITERAL_IF) {
            DetailAST rparen = ast.findFirstToken(RPAREN);
            return rparen != null ? rparen.getLineNo() : ast.getLineNo();
        }

        if (type == RECORD_DEF || type == CLASS_DEF || type == INTERFACE_DEF || type == ENUM_DEF) {
            DetailAST objBlock = ast.findFirstToken(OBJBLOCK);
            return objBlock != null ? objBlock.getLineNo() : AstUtil.findLastLine(ast);
        }

        if (type == LITERAL_TRY) {
            DetailAST slist = ast.findFirstToken(SLIST);
            return slist != null ? slist.getLineNo() : ast.getLineNo();
        }

        return AstUtil.findLastLine(ast);
    }

    private static boolean containsLongChain(DetailAST ast) {
        if (ast.getType() == METHOD_CALL) {
            return AstUtil.countMethodChain(ast) >= CHAIN_THRESHOLD;
        }
        DetailAST child = ast.getFirstChild();
        while (child != null) {
            if (containsLongChain(child)) {
                return true;
            }
            child = child.getNextSibling();
        }
        return false;
    }

    private static boolean containsLongChainInScope(DetailAST ast) {
        int type = ast.getType();
        if (type == CLASS_DEF || type == INTERFACE_DEF || type == ENUM_DEF) {
            return false;
        }
        if (type == LITERAL_TRY) {
            DetailAST resourceSpec = ast.findFirstToken(RESOURCE_SPECIFICATION);
            return resourceSpec != null && containsLongChain(resourceSpec);
        }
        return containsLongChain(ast);
    }

    private boolean tryHeaderFitsOnOneLine(DetailAST resource) {
        DetailAST spec = resource.getParent();
        if (spec == null || spec.getType() != RESOURCE_SPECIFICATION) {
            return false;
        }
        DetailAST tryAst = spec.getParent();
        if (tryAst == null || tryAst.getType() != LITERAL_TRY) {
            return false;
        }
        int firstLine = tryAst.getLineNo();
        DetailAST slist = tryAst.findFirstToken(SLIST);
        int lastLine = slist != null ? slist.getLineNo() : tryAst.getLineNo();
        return firstLine >= lastLine || buildCombinedLine(firstLine, lastLine).length() <= maxLineLength;
    }

    private int findSignatureLastLine(DetailAST def) {
        DetailAST slist = def.findFirstToken(SLIST);
        if (slist != null) {
            return slist.getLineNo();
        }
        DetailAST semi = def.findFirstToken(SEMI);
        if (semi != null) {
            return semi.getLineNo();
        }
        return AstUtil.findLastLine(def);
    }

    private boolean isNestedInCheckedParent(DetailAST ast) {
        DetailAST parent = ast.getParent();
        while (parent != null) {
            int parentType = parent.getType();
            if (parentType == METHOD_CALL || parentType == VARIABLE_DEF || parentType == RESOURCE || parentType == LITERAL_THROW || parentType == LITERAL_RETURN) {
                return true;
            }
            parent = parent.getParent();
        }
        return false;
    }

    private String buildCombinedLine(int firstLine, int lastLine) {
        String[] lines = getLines();
        String first = lines[firstLine - 1];
        int indent = first.length() - first.stripLeading().length();

        StringBuilder combined = new StringBuilder(first.stripTrailing());
        for (int i = firstLine; i < lastLine; i++) {
            String continuation = lines[i].strip();
            if (!continuation.isEmpty()) {
                if (needsSpace(combined, continuation)) {
                    combined.append(' ');
                }
                combined.append(continuation);
            }
        }

        return " ".repeat(indent) + combined.toString().stripLeading();
    }

    private static boolean needsSpace(StringBuilder sb, String next) {
        if (sb.isEmpty()) {
            return false;
        }
        char last = sb.charAt(sb.length() - 1);
        char first = next.charAt(0);
        if (first == ')' || first == '}' || first == ']') {
            return false;
        }
        if (last == '(' || last == '{' || last == '[') {
            return false;
        }
        return last != ' ';
    }
}
