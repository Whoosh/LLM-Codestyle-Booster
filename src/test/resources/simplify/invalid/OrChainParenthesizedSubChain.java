package simplify.invalid;

class OrChainParenthesizedSubChain {

    boolean parenSubChainOnRight(int n) {
        return n == 1 || (n == 2 || n == 3);
    }

    boolean parenSubChainOnLeft(int n) {
        return (n == 1 || n == 2) || n == 3;
    }

    boolean parenWrappingWholeChain(int n) {
        return (n == 1 || n == 2 || n == 3);
    }
}
