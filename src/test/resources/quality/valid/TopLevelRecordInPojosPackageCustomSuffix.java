package quality.valid.dto;

// Valid when packageSuffix=dto: the check should accept the alternative suffix.
public record TopLevelRecordInPojosPackageCustomSuffix(long id, String payload) {
}
