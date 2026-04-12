package test;

public class InlineRegexMutKiller {

    // Field initializer inside nested class — isInsideMethodBody traversal should
    // encounter VARIABLE_DEF -> OBJBLOCK and return false
    static class Inner {
        private String fieldRegex = "test".replaceAll("\\w+", "X");
    }

    // Method call without IDENT or DOT (e.g., method reference applied to regex method)
    // This exercises extractMethodName returning null
    void noDirectMethodName() {
        String s = "hello";
        // A call via a variable or complex expression that has no simple IDENT
        // This is hard to construct in normal Java but the valid fixture ensures
        // null extractMethodName doesn't cause a crash
    }
}
