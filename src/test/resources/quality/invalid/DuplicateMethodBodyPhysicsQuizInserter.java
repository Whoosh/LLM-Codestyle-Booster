package quality.invalid;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class DuplicateMethodBodyPhysicsQuizInserter {

    private static final String INSERT_SQL = "INSERT INTO quizzes VALUES (?, ?, ?)";

    private int batchSize() {
        return 500;
    }

    private void setQuizParams(PreparedStatement ps, Object quiz, long idx) throws SQLException {
        ps.setLong(1, idx);
    }

    private void executeBatch(PreparedStatement ps, int count) throws SQLException {
        if (count > 0) {
            ps.executeBatch();
        }
    }

    void insertBatch(Connection conn, List<Object> quizzes, long startIdx) throws SQLException, IOException {
        try (PreparedStatement ps = conn.prepareStatement(INSERT_SQL)) {
            int batchCount = 0;
            for (int i = 0; i < quizzes.size(); i++) {
                setQuizParams(ps, quizzes.get(i), startIdx + i);
                ps.addBatch();
                batchCount++;
                if (batchCount >= batchSize()) {
                    executeBatch(ps, batchCount);
                    batchCount = 0;
                }
            }
            executeBatch(ps, batchCount);
        }
    }
}
