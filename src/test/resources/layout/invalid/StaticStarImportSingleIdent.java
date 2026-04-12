package com.example;

// Tests extractParentClass L105 NO_COVERAGE: two-part static import (no nested DOT)
// For `import static SomeClass.MEMBER;` — the DOT's first child is IDENT not DOT
import static SomeClass.MEMBER;

@SuppressWarnings("unused")
public class StaticStarImportSingleIdent {
    int x = MEMBER;
}
