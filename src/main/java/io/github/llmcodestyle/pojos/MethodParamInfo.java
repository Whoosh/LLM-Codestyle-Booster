package io.github.llmcodestyle.pojos;

import java.util.List;
import java.util.Set;

/**
 * Describes parameter metadata for a single method or constructor: total count,
 * parameter names, and which indices are annotated {@code @Nullable} or accept
 * null based on a null-check in the body.
 */
public record MethodParamInfo(int paramCount, List<String> paramNames, Set<Integer> nullableIndices, Set<Integer> nullAcceptedIndices) {

    /**
     * Compact constructor — stores unmodifiable copies to prevent accidental mutation.
     */
    public MethodParamInfo {
        paramNames = List.copyOf(paramNames);
        nullableIndices = Set.copyOf(nullableIndices);
        nullAcceptedIndices = Set.copyOf(nullAcceptedIndices);
    }
}
