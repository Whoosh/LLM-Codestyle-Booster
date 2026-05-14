package com.example;

import java.io.IOException;
import java.sql.SQLException;

public class RepeatedExceptionWrappingMultiCatchInvalid {

    void methodA() {
        try {
            doIt("a");
        } catch (IOException | SQLException e) {
            throw new RuntimeException("oops", e);
        }
    }

    void methodB() {
        try {
            doIt("b");
        } catch (IOException | SQLException e) {
            throw new RuntimeException("oops", e);
        }
    }

    void methodC() {
        try {
            doIt("c");
        } catch (IOException | SQLException e) {
            throw new RuntimeException("oops", e);
        }
    }

    private void doIt(String name) throws IOException, SQLException {}
}
