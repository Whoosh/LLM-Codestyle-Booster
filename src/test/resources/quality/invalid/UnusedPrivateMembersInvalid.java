package test;

public class UnusedPrivateMembersInvalid {

    private int usedField = 0;
    private String unusedField = "never used";

    public int getUsedField() {
        return usedField;
    }

    private void unusedMethod() {
        System.out.println("nobody calls me");
    }

    public void publicMethod() {
    }
}
