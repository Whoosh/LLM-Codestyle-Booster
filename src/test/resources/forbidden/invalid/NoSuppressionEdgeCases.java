package test;

// NOPMD at file level
public class NoSuppressionEdgeCases {

    // CHECKSTYLE:OFF comment at class level
    String s = "value";

    // SUPPRESSFBWARNINGS in comment
    void method() {
        // regular code
    }

    @SuppressWarnings("unchecked")
    void annotated() { }

    @SuppressFBWarnings("NP")
    void fbAnnotated() { }
}
