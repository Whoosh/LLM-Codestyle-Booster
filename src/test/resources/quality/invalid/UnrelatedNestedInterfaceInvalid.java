package quality.invalid;

public class UnrelatedNestedInterfaceInvalid {

    private String name;

    public String getName() {
        return name;
    }

    // Abstract-only interface, no outer references → violation
    interface Formatter {

        String format(String input);
    }

    // Interface inside nested class → violation
    static class Holder {

        int value;

        interface Callback {

            void onComplete(int result);
        }
    }
}
