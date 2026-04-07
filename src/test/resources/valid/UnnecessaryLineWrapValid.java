package com.example;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.logging.Logger;

public class UnnecessaryLineWrapValid {

    private static final Logger LOG = Logger.getLogger("test");

    // Case 1: genuinely long method call — combined exceeds 180 chars
    void longMethodCall() {
        LOG.info(String.format("Processing batch %s with very long description that makes this absolutely impossible to fit within one hundred eighty characters when combined together with arguments",
                "batchId", "extra1", "extra2", "extra3", "extra4"));
    }

    // Case 2: single-line — no wrap
    void singleLine() {
        LOG.info("simple message");
    }

    // Case 3: already on one line
    void oneLiner() {
        String result = computeValue();
    }

    // Case 4: method declaration on one line
    public void shortMethod(String arg) {
        // body
    }

    // Case 5: genuinely long method signature exceeding 180
    public void methodWithManyParametersExceedingOneHundredEightyCharsWhenCombinedOnASingleLine(String firstParameterName, String secondParameterName,
            String thirdParameterName, String fourthParameterName) {
        // body
    }

    // Case 6: genuinely long if-condition (combined > 180)
    void longIfCondition() {
        boolean veryLongVariableNameThatTakesUpLotsOfSpaceInCode = true;
        boolean anotherExtremelyLongBooleanVariableNameForTestingPurposesHere = false;
        boolean yetAnotherVeryLongVariableNameToMakeThisExceedTheLimitDefinitely = true;
        if (veryLongVariableNameThatTakesUpLotsOfSpaceInCode || anotherExtremelyLongBooleanVariableNameForTestingPurposesHere
                || yetAnotherVeryLongVariableNameToMakeThisExceedTheLimitDefinitely) {
            // body
        }
    }

    // Case 7: throw with genuinely long message — combined exceeds 180
    void throwLong(String field) {
        throw new IllegalStateException(
                "Invalid translations resource: the object field named '" + field + "' is missing from the configuration and cannot be resolved automatically by the system at runtime");
    }

    // Case 8: return with genuinely long expression — combined exceeds 180
    String returnLong(String firstParameterName, String secondParameterName) {
        return "This is a very long return expression that absolutely cannot fit within one hundred eighty characters when combined with the variable references: " + firstParameterName
                + " and " + secondParameterName;
    }

    // Case 9: try-with-resources exceeding 180 chars when combined
    void longTryResource() throws Exception {
        try (AutoCloseable firstStatementWithVeryLongName = createFirstConnectionAndPrepareInsertStatementWithParams("firstLongParam", "secondLong");
             AutoCloseable secondStatementWithVeryLongName = createSecondConnectionAndPrepareUpdateStatementWithParams("thirdLongParam", "fourthP")) {
            // body
        }
    }

    // Case 10: interface extends exceeding 180 chars when combined
    interface VeryLongNamedExtractorInterfaceForMultipleDocumentFormatsIncludingPdfAndDjvu
            extends AutoCloseable, Comparable<VeryLongNamedExtractorInterfaceForMultipleDocumentFormatsIncludingPdfAndDjvu> {
    }

    // Case 11: class already on one line — no wrap
    static class SimpleNested extends Thread {
    }

    private String computeValue() {
        return "value";
    }

    private AutoCloseable createFirstConnectionAndPrepareInsertStatementWithParams(String a, String b) {
        return null;
    }

    private AutoCloseable createSecondConnectionAndPrepareUpdateStatementWithParams(String a, String b) {
        return null;
    }
}
