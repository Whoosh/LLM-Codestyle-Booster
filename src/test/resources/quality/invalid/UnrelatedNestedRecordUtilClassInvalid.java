package quality.invalid;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

// Mirrors a typical util class that carries a results POJO as a nested record at the
// bottom — the exact shape of JavaBatchValidationUtil.ValidationResult. The record
// touches no outer state and must be flagged for extraction to a pojos package.
public final class UnrelatedNestedRecordUtilClassInvalid {

    private static final Logger LOG = Logger.getLogger(UnrelatedNestedRecordUtilClassInvalid.class.getName());
    private static final String EMPTY = "";
    private static final String JSON_RESULT = "result";
    private static final String[] REQUIRED_FIELDS = {"topic", "body"};

    private UnrelatedNestedRecordUtilClassInvalid() {
    }

    static ValidationResult validateFile(String path) throws IOException {
        LOG.info("validating " + path);
        List<String> valid = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        for (String field : REQUIRED_FIELDS) {
            if (field.isBlank()) {
                errors.add(JSON_RESULT + EMPTY);
            }
        }
        return new ValidationResult(valid.size(), valid, errors);
    }

    record ValidationResult(int total, List<String> valid, List<String> errors) {
    }
}
