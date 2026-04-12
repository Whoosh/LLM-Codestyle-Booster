package test;

public class InlineRegexFieldLevel {

    // Field-level regex usage — isInsideMethodBody returns false so no violation
    // This exercises isInsideMethodBody line 56-58 (VARIABLE_DEF with OBJBLOCK grandparent)
    private String splitResult = "hello world".replaceAll("\\s+", "-");
}
