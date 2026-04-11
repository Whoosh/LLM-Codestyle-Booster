package io.github.llmcodestyle.pojos;

/**
 * First-seen location of a normalized method body: enclosing class, method name, and
 * whether the body is stateless (independent of the enclosing type's instance state).
 */
public record DuplicateMethodOccurrence(String className, String methodName, boolean stateless) {
}
