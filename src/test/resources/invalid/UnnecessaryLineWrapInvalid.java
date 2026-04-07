package com.example;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.logging.Logger;

public class UnnecessaryLineWrapInvalid {

    private static final Logger LOG = Logger.getLogger("test");
    private static final String TABLE = "example.tbl_data";

    // Case 1: method call with short args
    void methodCallShort() {
        LOG.info(String.format("Batch %s complete",
                "batchId"));
    }

    // Case 2: assignment that fits
    void assignmentShort() {
        String result =
                computeValue();
    }

    // Case 3: method declaration with params that fits
    public void process(String conn,
                        String table) {
        // body
    }

    // Case 4: method with throws on separate line
    private static void processRows(ResultSet rs, Connection conn,
                                     boolean apply, int stats)
            throws Exception {
        // body
    }

    // Case 5: if-condition that fits on one line
    void ifCondition() {
        boolean a = true;
        boolean b = false;
        boolean c = true;
        if (a || b
                || c) {
            // body
        }
    }

    // Case 6: record that fits on one line
    private record MigrationResult(
            List<String> bodyParts,
            List<String> explParts,
            List<String> usageParts) {
    }

    // Case 7: try-with-resources where resource fits
    void tryResource(Connection conn) throws Exception {
        try (Statement stmt =
                     conn.createStatement()) {
            // body
        }
    }

    // Case 8: variable with string concat that fits
    void stringConcat() {
        String sql = "SELECT * FROM " +
                "table";
    }

    // Case 9: throw with short constructor call
    void throwShort(String field) {
        throw new IllegalStateException(
                "Missing field '" + field + "'");
    }

    // Case 10: return with short expression
    String returnShort() {
        return "prefix"
                + "_suffix";
    }

    // Case 11: try-with-resources with multiple resources that fit on one line
    void tryMultiResource() throws Exception {
        try (AutoCloseable first = open();
             AutoCloseable second = prepare()) {
            // body
        }
    }

    // Case 12: interface with extends that fits on one line
    interface ShortExtractor
            extends AutoCloseable {
    }

    // Case 13: class with extends that fits on one line
    static abstract class BaseProcessor
            extends Thread {
    }

    private String computeValue() {
        return "value";
    }

    private AutoCloseable open() {
        return null;
    }

    private AutoCloseable prepare() {
        return null;
    }
}
