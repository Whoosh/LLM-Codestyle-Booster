package io.github.llmcodestyle.pojos;

import java.util.Set;

/**
 * Instance scope of an enclosing type, split into the buckets needed for the
 * instance-call heuristic used by {@code MethodMayBeStaticCheck} and
 * {@code DuplicateMethodBodyCheck}.
 *
 * @param instanceFields non-static fields (for records, also the components)
 * @param instanceMethods non-static methods declared directly on the type
 * @param declaredMethods all methods declared directly on the type (static and instance)
 */
public record InstanceScope(Set<String> instanceFields, Set<String> instanceMethods, Set<String> declaredMethods) {

    public InstanceScope {
        instanceFields = Set.copyOf(instanceFields);
        instanceMethods = Set.copyOf(instanceMethods);
        declaredMethods = Set.copyOf(declaredMethods);
    }
}
