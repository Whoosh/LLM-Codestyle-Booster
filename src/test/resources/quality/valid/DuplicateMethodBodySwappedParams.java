package quality.valid;

public class DuplicateMethodBodySwappedParams {

    // These two methods have the same structure but use parameters in different order.
    // With correct normalization ($n0, $n1), they produce different fingerprints.
    // If the placeholder is always "" (EMPTY_RETURNS mutation on assignIfAbsent),
    // both would serialize identically, producing a false positive.
    public String concat(String first, String second) {
        if (first == null) {
            return second;
        }
        if (second == null) {
            return first;
        }
        return first + second;
    }

    public String concatReversed(String alpha, String beta) {
        if (alpha == null) {
            return beta;
        }
        if (beta == null) {
            return alpha;
        }
        return beta + alpha;
    }
}
