package com.example;

import java.io.Closeable;

// Targets the remaining surviving mutations in UnnecessaryLineWrapCheck
@SuppressWarnings("unused")
public abstract class UnnecessaryLineWrapMutationKiller3 {

    // L83 shouldSkip: 2S - `type == RESOURCE && tryHeaderFitsOnOneLine(ast)`
    // When negating `type == RESOURCE`, non-RESOURCE types go through tryHeaderFitsOnOneLine.
    // We need a RESOURCE wrapped where try-header does NOT fit => should produce a violation.
    void resourceWrappedTryHeaderDoesNotFit() throws Exception {
        try (Closeable res =
                 open()) {
            use(res);
        }
    }

    // L94 computeFirstLine: 1S - `if (ident != null)` in the DEFS_WITH_TYPE_OR_IDENT_FIRST branch
    // COMPACT_CTOR_DEF has no TYPE and no IDENT (both null) — falls to ast.getLineNo()
    // We need a CTOR_DEF (which IS in DEFS_WITH_TYPE_OR_IDENT_FIRST) where TYPE is null
    // CTOR_DEF has no TYPE token. It does have IDENT. We need one that wraps.
    UnnecessaryLineWrapMutationKiller3(
            String param) {
    }

    // L106 firstNonAnnotationLine: 1S - `if (modifiers != null)`
    // Container type with NO modifiers at all (bare class). Rare but possible for inner classes.
    // Actually, for a nested class/interface with wrapping, modifiers are always present (even empty).
    // The surviving mutation on `modifiers != null` means when negated, we'd skip to IDENT.
    // We need a test where having modifiers vs skipping to IDENT gives different start lines.
    // An inner class with `static` modifier, where `static` is on a different line than IDENT:
    static
    class SplitModifierClass extends Thread {
    }

    // L172 tryHeaderFitsOnOneLine: 2S - `spec == null || spec.getType() != RESOURCE_SPECIFICATION`
    // The only way to call tryHeaderFitsOnOneLine is when `type == RESOURCE`.
    // The parent of the RESOURCE is always RESOURCE_SPECIFICATION. So spec != null and type == RESOURCE_SPECIFICATION.
    // Negating either condition should not change behavior for normal code -> EQUIVALENT MUTANT?
    // Actually no: the negation of `spec == null` changes `||` short-circuit. If spec is NOT null,
    // negating `spec == null` makes it `true`, so the method returns false. This changes behavior:
    // RESOURCE nodes that would normally be checked are now wrongly skipped.
    // We need a RESOURCE that WRAPS (multi-line) where try-header-fits should return TRUE
    // (meaning the resource should be skipped). If tryHeaderFitsOnOneLine returns false instead
    // (because of the negated mutation), the RESOURCE would NOT be skipped and WOULD produce a violation.

    // L191 findSignatureLastLine: 1S - `if (semi != null)` for abstract methods
    // Need abstract method where semi is on a different line from slist (which is null for abstract).
    abstract void process(String a,
                          String b);

    // L217 buildCombinedLine: 1S - `if (needsSpace(combined, continuation))`
    // If needsSpace is negated: spaces would NOT be inserted where needed, making combined shorter.
    // Need: a wrapping that just barely fits within maxLineLength WITH the space, but without space
    // it would be shorter. Since the check fires when combined <= max, negating needsSpace would
    // make it think the line is even shorter (missing a space) -> still fires. The test would NOT
    // notice a difference unless we check the exact char count in the message.
    // Actually: if needsSpace is negated, spaces are NOT inserted, so words run together.
    // This changes the combined length reported in the message.
    void needsSpaceMatters() {
        String result =
                compute("test");
    }

    // L228 needsSpace: 1S - `if (sb.isEmpty())` -> if sb IS empty, return false (no space needed)
    // Negating: when sb is empty, we DON'T return false, so we continue and might add a space.
    // We need an empty StringBuilder scenario. This happens when the first line after stripping
    // is empty. Let's create a variable def where the first line is just whitespace:
    // Actually, sb starts from `first.stripTrailing()`, so only empty if the first source line is blank.
    // That's unlikely. The mutation probably survives because this branch is rarely hit.

    // L232 needsSpace: 2S - `CLOSING_BRACKETS.contains(next.charAt(0)) || OPENING_BRACKETS.contains(last)`
    // Negating CLOSING_BRACKETS check: a closing bracket line gets an unwanted space before it.
    // Negating OPENING_BRACKETS check: after opening bracket, space is added when it shouldn't be.
    // We need to assert EXACT combined length to detect the extra/missing space.

    // L235 needsSpace: 1S - `return last != ' '`
    // Negating: when last IS a space, we return true (add space) instead of false (don't add space).
    // This would add a double space. We need to detect the exact combined length.

    private Closeable open() { return null; }
    private void use(Closeable c) { }
    private String compute(String s) { return s; }
}
