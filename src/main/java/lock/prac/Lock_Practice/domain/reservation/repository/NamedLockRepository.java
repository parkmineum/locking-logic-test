package lock.prac.Lock_Practice.domain.reservation.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

//@Repository
//@RequiredArgsConstructor
//public class NamedLockRepository {
//
//    private final JdbcTemplate jdbcTemplate;
//
//    public boolean getLock(String key, int timeoutSeconds) {
//        String sql = "SELECT GET_LOCK(?, ?)";
//        Boolean result = jdbcTemplate.queryForObject(sql, Boolean.class, key, timeoutSeconds);
//        return Boolean.TRUE.equals(result);
//    }
//
//    public void releaseLock(String key) {
//        jdbcTemplate.update("DO RELEASE_LOCK(?)", key);
//    }
//}

@Repository
@RequiredArgsConstructor
public class NamedLockRepository {

    private final DataSource dataSource;

    public boolean getLock(String key, int timeoutSeconds) {
        try (Connection conn = DataSourceUtils.getConnection(dataSource);
             PreparedStatement stmt = conn.prepareStatement("SELECT GET_LOCK(?, ?)")) {

            stmt.setString(1, key);
            stmt.setInt(2, timeoutSeconds);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getBoolean(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("GET_LOCK 실패", e);
        }
    }

    public void releaseLock(String key) {
        try (Connection conn = DataSourceUtils.getConnection(dataSource);
             PreparedStatement stmt = conn.prepareStatement("DO RELEASE_LOCK(?)")) {
            stmt.setString(1, key);
            stmt.execute();
        } catch (SQLException e) {
            throw new RuntimeException("RELEASE_LOCK 실패", e);
        }
    }
}

