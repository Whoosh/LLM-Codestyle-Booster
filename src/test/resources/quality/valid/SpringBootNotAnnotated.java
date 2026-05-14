package quality.valid;

public class SpringBootNotAnnotated {

    static void main(String[] args) {
        sink("Just a regular class");
    }

    void otherMethod() {
        sink("not main");
    }

    private static void sink(Object x) {
        if (x.hashCode() == Integer.MIN_VALUE) {
            throw new IllegalStateException();
        }
    }
}
