package quality.invalid;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

public final class DuplicateMethodBodyJavaBatchInserter {

    private static final Object MAPPER = new Object();

    private DuplicateMethodBodyJavaBatchInserter() {
    }

    static List<Object> readJsonLines(Path path) throws IOException {
        List<Object> nodes = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(path, UTF_8)) {
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                if (!line.isBlank()) {
                    nodes.add(MAPPER.toString() + line);
                }
            }
        }
        return nodes;
    }
}
