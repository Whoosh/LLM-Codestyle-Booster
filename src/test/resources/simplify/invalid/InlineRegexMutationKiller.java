package test;

public class InlineRegexMutationKiller {

    // Field initializer using regex method — isInsideMethodBody should return false
    // because parent is VARIABLE_DEF and grandparent is OBJBLOCK
    // Line 56-58 SURVIVED: both the VARIABLE_DEF and OBJBLOCK checks
    private String fieldSplit = "hello world".split("\\s+")[0];

    // Method body with qualified call: obj.matches("regex")
    // This exercises extractMethodName with DOT (line 68-70)
    void qualifiedCall() {
        String s = "hello";
        boolean b = s.matches("\\d+");
    }

    // Method body with unqualified call to replaceAll
    void unqualifiedCall(String input) {
        String result = input.replaceAll("\\w+", "X");
    }

    // Local variable initializer with regex — isInsideMethodBody should return true
    // because after walking up from VARIABLE_DEF, grandparent is SLIST not OBJBLOCK
    void localVarWithRegex() {
        String localSplit = "a,b,c".split(",")[0];
    }
}
