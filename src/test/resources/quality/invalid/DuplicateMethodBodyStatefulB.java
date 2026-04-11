package quality.invalid;

public class DuplicateMethodBodyStatefulB {

    private int discount;
    private int taxRate;

    public int computePrice(int amount) {
        int result = amount - discount;
        result = result * taxRate;
        return result;
    }
}
