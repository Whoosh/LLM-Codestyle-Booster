package io.github.llmcodestyle.pojos;

/**
 * Per-class method count buckets used by {@code UtilClassNamingCheck} to decide
 * whether a class is "util-shaped" (all methods static, at least one public).
 *
 * @param total all method declarations in the class body (excluding constructors)
 * @param staticCount methods carrying the {@code static} modifier
 * @param publicCount methods carrying the {@code public} modifier
 */
public record MethodCounts(int total, int staticCount, int publicCount) {
}
