package test;

import java.nio.file.Path;

public record CollapsibleRecordMethodRun(Path outputRoot) {

    static final String DEFAULT_TABLE = "book_problems";
    private static final String COL_SEPARATOR = "', '";
    private static final String INSERT_PREFIX = "INSERT INTO ";
    private static final String VALUES_COLS = " (text, library, book_name, rest_text) VALUES\n";

    static String buildRestTextSql(String bookName, String library, String restText) {
        return INSERT_PREFIX + DEFAULT_TABLE + VALUES_COLS + "(''" + COL_SEPARATOR + escapeSql(library) + COL_SEPARATOR
            + escapeSql(bookName) + COL_SEPARATOR + escapeSql(restText) + "');\n";
    }

    static String escapeSql(String value) {
        return value.replace("'", "\\'");
    }
}
