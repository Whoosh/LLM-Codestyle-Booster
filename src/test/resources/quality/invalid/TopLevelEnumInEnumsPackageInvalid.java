package quality.invalid;

// ERROR: top-level enum in a non-enums package.
public enum TopLevelEnumInEnumsPackageInvalid {
    ALPHA, BETA, GAMMA
}

// ERROR: another top-level enum in the same (non-enums) file.
enum HttpStatus {
    OK, NOT_FOUND, INTERNAL_ERROR
}

// ERROR: a third one to exercise multi-enum files seen in real projects.
enum Direction {
    NORTH, SOUTH, EAST, WEST
}
