package quality.invalid;

import java.io.IOException;
import java.io.InputStream;

import static java.nio.charset.StandardCharsets.UTF_8;

public final class DuplicateMethodBodyPhysicsPromptBuilder {

    private final String systemPromptText = loadResource("batch/system-prompt.md");

    public String systemPrompt() {
        return systemPromptText;
    }

    private static String loadResource(String path) {
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(path)) {
            if (is == null) {
                throw new IllegalStateException("Resource not found: " + path);
            }
            return new String(is.readAllBytes(), UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load resource: " + path, e);
        }
    }
}
