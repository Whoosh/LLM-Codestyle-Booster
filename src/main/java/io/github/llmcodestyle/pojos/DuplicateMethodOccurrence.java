package io.github.llmcodestyle.pojos;

/**
 * First-seen location of a normalized method body: enclosing class and method name.
 */
public record DuplicateMethodOccurrence(String className, String methodName) {
}
