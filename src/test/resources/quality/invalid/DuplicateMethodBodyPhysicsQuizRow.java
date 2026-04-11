package quality.invalid;

import java.util.List;

public class DuplicateMethodBodyPhysicsQuizRow {

    private static final Object MAPPER = new Object();
    private static final Object LIST_OF_STRING = new Object();

    private static List<String> parseAnswers(String input) {
        if (input == null || input.isBlank()) {
            return List.of();
        }
        String normalized = input.trim();
        if (normalized.startsWith("[")) {
            try {
                return readValue(normalized, LIST_OF_STRING);
            } catch (RuntimeException ex) {
                throw new IllegalArgumentException("Invalid JSON array: " + normalized, ex);
            }
        }
        return List.of(normalized);
    }

    private static List<String> readValue(String raw, Object type) {
        return List.of(raw);
    }
}
