package io.github.llmcodestyle.layout;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;

import static com.puppycrawl.tools.checkstyle.api.TokenTypes.*;
import static io.github.llmcodestyle.utils.AstMethodCallUtil.*;
import static io.github.llmcodestyle.utils.AstUtil.*;

import java.util.Set;

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

    private static final Set<Character> CLOSING_BRACKETS = Set.of(')', '}', ']');
    private static final Set<Character> OPENING_BRACKETS = Set.of('(', '{', '[');

    private static final Set<Integer> DEFS_WITH_TYPE_OR_IDENT_FIRST = Set.of(METHOD_DEF, CTOR_DEF, COMPACT_CTOR_DEF, RECORD_DEF, VARIABLE_DEF, RESOURCE);
    private static final Set<Integer> CONTAINER_TYPE_DEFS = Set.of(CLASS_DEF, INTERFACE_DEF, ENUM_DEF);
    private static final Set<Integer> CONTAINER_OR_RECORD_DEFS = Set.of(RECORD_DEF, CLASS_DEF, INTERFACE_DEF, ENUM_DEF);
    private static final Set<Integer> METHOD_SIGNATURE_DEFS = Set.of(METHOD_DEF, CTOR_DEF, COMPACT_CTOR_DEF);
    private static final Set<Integer> CHECKED_PARENT_TYPES = Set.of(METHOD_CALL, VARIABLE_DEF, RESOURCE, LITERAL_THROW, LITERAL_RETURN);

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
        if (shouldSkip(ast)) {
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

    private boolean shouldSkip(DetailAST ast) {
        if (isNestedInCheckedParent(ast) || containsLongChainInScope(ast)) {
            return true;
        }
        int type = ast.getType();
        if (type == LITERAL_TRY) {
            return ast.findFirstToken(RESOURCE_SPECIFICATION) == null;
        }
        return type == RESOURCE && tryHeaderFitsOnOneLine(ast);
    }

    private static int computeFirstLine(DetailAST ast) {
        int type = ast.getType();
        if (DEFS_WITH_TYPE_OR_IDENT_FIRST.contains(type)) {
            DetailAST typeToken = ast.findFirstToken(TYPE);
            if (typeToken != null) {
                return typeToken.getLineNo();
            }
            DetailAST ident = ast.findFirstToken(IDENT);
            if (ident != null) {
                return ident.getLineNo();
            }
        }
        if (CONTAINER_TYPE_DEFS.contains(type)) {
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

    private static int computeLastLine(DetailAST ast) {
        int type = ast.getType();

        if (METHOD_SIGNATURE_DEFS.contains(type)) {
            return findSignatureLastLine(ast);
        }

        if (type == LITERAL_IF) {
            DetailAST rparen = ast.findFirstToken(RPAREN);
            return rparen != null ? rparen.getLineNo() : ast.getLineNo();
        }

        if (CONTAINER_OR_RECORD_DEFS.contains(type)) {
            DetailAST objBlock = ast.findFirstToken(OBJBLOCK);
            return objBlock != null ? objBlock.getLineNo() : findLastLine(ast);
        }

        if (type == LITERAL_TRY) {
            DetailAST slist = ast.findFirstToken(SLIST);
            return slist != null ? slist.getLineNo() : ast.getLineNo();
        }

        return findLastLine(ast);
    }

    private static boolean containsLongChain(DetailAST ast) {
        if (ast.getType() == METHOD_CALL) {
            return countMethodChain(ast) >= CHAIN_THRESHOLD;
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
        if (CONTAINER_TYPE_DEFS.contains(type)) {
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

    private static int findSignatureLastLine(DetailAST def) {
        DetailAST slist = def.findFirstToken(SLIST);
        if (slist != null) {
            return slist.getLineNo();
        }
        DetailAST semi = def.findFirstToken(SEMI);
        if (semi != null) {
            return semi.getLineNo();
        }
        return findLastLine(def);
    }

    private static boolean isNestedInCheckedParent(DetailAST ast) {
        DetailAST parent = ast.getParent();
        while (parent != null) {
            if (CHECKED_PARENT_TYPES.contains(parent.getType())) {
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
        if (CLOSING_BRACKETS.contains(next.charAt(0)) || OPENING_BRACKETS.contains(last)) {
            return false;
        }
        return last != ' ';
    }
}
