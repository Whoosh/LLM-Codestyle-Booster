package simplify.valid;

public class OrChainUpperSnakeEdgeValid {

    // Constants with only underscores and digits (no letters) should NOT be treated as UPPER_SNAKE_CASE
    // This exercises the hasLetter check in isUpperSnakeCase
    private static final int _1 = 1;
    private static final int _2 = 2;
    private static final int _3 = 3;

    // Chain with lowercase identifiers as RHS (not UPPER_SNAKE_CASE)
    public boolean lowercaseRhs(int type) {
        int alpha = type + _1;
        int beta = type + _2;
        int gamma = type + _3;
        return (type == alpha || type == beta || type == gamma) && (alpha + beta + gamma > 0);
    }
}
