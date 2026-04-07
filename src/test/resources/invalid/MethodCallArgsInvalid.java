package test;

public class MethodCallArgsInvalid {

    public void badMixedArgs() {
        // a and b on same line, c on next — mixed!
        process("alpha", "beta",
            "gamma");
    }

    public void anotherBadCase() {
        // first two args same line, third different
        build("key", "value",
            42);
    }

    private void process(String a, String b, String c) {
    }

    private void build(String k, String v, int n) {
    }
}
