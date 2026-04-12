package simplify.invalid;

public class OrChainUpperSnakeEdge {

    // Constants with digits (for isUpperSnakeCase coverage)
    private static final int TYPE_1 = 1;
    private static final int TYPE_2 = 2;
    private static final int TYPE_3 = 3;

    // Chain using constants whose names contain digits
    public boolean digitConstants(int type) {
        return type == TYPE_1 || type == TYPE_2 || type == TYPE_3;
    }
}
