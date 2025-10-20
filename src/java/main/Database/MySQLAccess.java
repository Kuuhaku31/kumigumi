// MySQLAccess.java


package Database;

import utils.TableData;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;

public
class MySQLAccess
{
    private Connection conn;

    /**
     * 构建 INSERT ... ON DUPLICATE KEY UPDATE SQL 模板
     */
    private static
    String BuildUpsertSQL(String table_name, ArrayList<String> headers)
    {
        String columns      = String.join(", ", headers);
        String placeholders = String.join(", ", Collections.nCopies(headers.size(), "?"));
        String update_part = headers.stream()
            .map(h -> h + " = VALUES(" + h + ")")
            .collect(Collectors.joining(", "));

        return String.format(
            "INSERT INTO %s (%s) VALUES (%s) ON DUPLICATE KEY UPDATE %s",
            table_name, columns, placeholders, update_part
        );
    }

    public
    void Open() throws SQLException
    {
        String database_url  = "jdbc:mysql://localhost:3706/";
        String database_name = "kumigumi-new";
        String url           = database_url + database_name + "?allowPublicKeyRetrieval=true&useSSL=false";
        String username      = "root";
        String password      = "root-password";

        conn = DriverManager.getConnection(url, username, password);
    }

    public
    void Close() throws SQLException
    {
        if(conn != null && !conn.isClosed()) conn.close();
    }

    // 插入更新表格
    public
    void Upsert(ArrayList<TableData> table_data_list) throws SQLException
    {
        for(TableData table_data : table_data_list) Upsert(table_data);
    }

    // 插入更新表格
    public
    void Upsert(TableData table_data) throws SQLException
    {
        String            tableName    = table_data.table_name();
        ArrayList<String> headers      = table_data.headers();
        int               column_count = headers.size();

        // 1️⃣ 构建通用 SQL 语句模板
        String sql = BuildUpsertSQL(tableName, headers);
        try(PreparedStatement stmt = conn.prepareStatement(sql))
        {
            conn.setAutoCommit(false); // 启用事务

            // 2️⃣ 遍历所有行数据
            for(ArrayList<String> row_data : table_data.data())
            {
                for(int i = 0; i < column_count; i++) stmt.setString(i + 1, row_data.get(i));
                stmt.addBatch(); // 加入批量
            }

            // 3️⃣ 执行批处理
            stmt.executeBatch();
            conn.commit();
        }
        catch(SQLException e)
        {
            conn.rollback(); // 回滚事务
            throw e;
        }
        finally
        {
            conn.setAutoCommit(true);
        }

    }

}
