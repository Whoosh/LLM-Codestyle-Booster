package test;

public class MethodCallArgsValid {

    public void allOnOneLine() {
        process("alpha", "beta", "gamma");
    }

    public void oneArgPerLine() {
        process(
            "alpha",
            "beta",
            "gamma");
    }

    public void singleArgMultiLine() {
        process(
            "only one arg here");
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
