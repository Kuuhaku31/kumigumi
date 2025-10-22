// MySQLAccess.java


package Database;

import utils.TableData;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;

public
class MySQLAccess
{
    private Connection conn            = null;
    private String[]   table_name_list = new String[0];

    public
    void Open() throws SQLException
    {
        if(conn != null) return;

        String database_url  = "jdbc:mysql://localhost:3706/";
        String database_name = "kumigumi-new";
        String url           = database_url + database_name + "?allowPublicKeyRetrieval=true&useSSL=false";
        String username      = "root";
        String password      = "root-password";

        conn = DriverManager.getConnection(url, username, password);

        // 初始化 table_name_list
        try(var stmt = conn.createStatement(); var rs = stmt.executeQuery("SHOW TABLES"))
        {
            ArrayList<String> table_names = new ArrayList<>();
            while(rs.next()) table_names.add(rs.getString(1));

            table_name_list = table_names.toArray(new String[0]); // 转换为数组
        }
    }

    public
    void Close() throws SQLException
    {
        table_name_list = new String[0];
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
        // 检查 table_name 合法性
        String table_name = null;
        for(var name : table_name_list) if(name.equals(table_data.table_name())) table_name = name;
        if(table_name == null) throw new IllegalArgumentException("Invalid table name: " + table_data.table_name());


        if(!table_name.matches("[A-Za-z0-9_]+"))
            throw new IllegalArgumentException("Invalid SQL identifier: " + table_name);

        // 1️⃣ 构建通用 SQL 语句模板
        String columns      = String.join("`, `", table_data.headers());
        String placeholders = String.join(", ", Collections.nCopies(table_data.headers().length, "?"));
        String update_part = String.join(", ",
            java.util.Arrays.stream(table_data.headers())
                .map(h -> "`" + h + "`" + " = new." + h)
                .toArray(String[]::new)
        );

        String sql = String.format(
            """
            INSERT INTO `%s` (`%s`)
            VALUES (%s) AS `new`
            ON DUPLICATE KEY UPDATE %s
            """,
            table_name, columns, placeholders, update_part
        );

        try(var stmt = conn.prepareStatement(sql))
        {
            conn.setAutoCommit(false); // 启用事务

            // 2️⃣ 遍历所有行数据
            for(String[] row_data : table_data.data())
            {
                for(int i = 0; i < table_data.headers().length; i++) stmt.setString(i + 1, row_data[i]);
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
