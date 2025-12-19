package Database;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import Database.InfoItem.InfoItem;

public class SQLiteAccess implements Closeable {

    private Connection conn;

    public SQLiteAccess(String db_url) throws SQLException {
        conn = DriverManager.getConnection(db_url);
    }

    public void Upsert(List<InfoItem> items) {

    }

    @Override
    public void close() throws IOException {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                throw new IOException("Failed to close database connection", e);
            } finally {
                conn = null;
            }
        }
    }

}
