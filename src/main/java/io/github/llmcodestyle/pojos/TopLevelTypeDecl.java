package io.github.llmcodestyle.pojos;

/**
 * Pure data carrier for a top-level Java type declaration: its kind ({@code class},
 * {@code record}, {@code enum}, {@code interface}) and simple name.
 */
public record TopLevelTypeDecl(String kind, String name) {
}
