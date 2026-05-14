package test;

public class MethodCallArgsValid {

    public void allOnOneLine() {
        process("alpha", "beta", "gamma");
    }

    public void oneArgPerLine() {
        process(
            "an argument that is reasonably long to ensure that joining all of them would exceed the eighty char limit",
            "another argument that is also reasonably long to make sure UnnecessaryLineWrapCheck does not flag this multiline form",
            "a third argument that pushes the joined form well past the one hundred eighty character threshold so the wrap is necessary");
    }

    public void singleArgMultiLine() {
        process(
            "only one arg here that is sufficiently long so the wrapped form is justified compared to keeping the call on one line that would otherwise become too long for a sensible eighty char limit");
    }

    public void twoArgsSameLine() {
        build("key", "value");
    }

    private void process(String a, String b, String c) {
    }

    private void process(String a) {
    }

    private void build(String k, String v) {
    }
}
