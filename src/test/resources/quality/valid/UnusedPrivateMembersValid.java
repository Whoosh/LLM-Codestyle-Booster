package test;

public class UnusedPrivateMembersValid {

    private int usedField = 0;
    private String alsoUsed = "used";

    public int getUsedField() {
        return usedField;
    }

    public String getAlsoUsed() {
        return alsoUsed;
    }

    private void usedHelper() {
        System.out.println("called by public method");
    }

    public void publicMethod() {
        usedHelper();
    }

    // Regression: inner class's private member referenced from OUTER must not fire
    public int useInnerConstant() {
        return Inner.INNER_VALUE;
    }

    private static final class Inner {
        private static final int INNER_VALUE = 42;
    }
}
