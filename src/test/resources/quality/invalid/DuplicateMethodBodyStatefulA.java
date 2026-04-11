package quality.invalid;

public class DuplicateMethodBodyStatefulA {

    private int discount;
    private int taxRate;

    public int computeDiscount(int base) {
        int adjusted = base - discount;
        adjusted = adjusted * taxRate;
        return adjusted;
    }
}
