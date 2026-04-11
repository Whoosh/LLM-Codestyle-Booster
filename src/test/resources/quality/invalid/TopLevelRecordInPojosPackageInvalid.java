package quality.invalid;

// ERROR: top-level record in a non-pojos package.
public record TopLevelRecordInPojosPackageInvalid(String input, String output) {
}

// ERROR: another top-level record in the same (non-pojos) file.
record LocalFileWriter(String path) {
}

// ERROR: a third one to exercise multi-record files seen in real projects.
record ProblemPayload(int id, String title) {
}
