package io.github.llmcodestyle.pojos;

/**
 * Pure data carrier for a public method's name and line number, used by coverage checks.
 */
public record MethodInfo(String name, int line) {
}
