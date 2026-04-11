package quality.invalid;

import java.util.List;

public class DuplicateMethodBodyJsonArrays {

    private static final Object MAPPER = new Object();
    private static final Object LIST_OF_STRING = new Object();

    public static List<String> parseTags(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        String trimmed = raw.trim();
        if (trimmed.startsWith("[")) {
            try {
                return readValue(trimmed, LIST_OF_STRING);
            } catch (RuntimeException e) {
                throw new IllegalArgumentException("Invalid JSON array: " + trimmed, e);
            }
        }
        return List.of(trimmed);
    }

    private static List<String> readValue(String raw, Object type) {
        return List.of(raw);
    }
}
