// MySQLAccess.java


package Database;

import Excel.TableData;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;

public
class MySQLAccess
{
    private Connection conn;

    public
    void Open() throws SQLException
    {
        String database_url  = "jdbc:mysql://localhost:3306/";
        String database_name = "kumigumi-new";
        String url           = database_url + database_name + "?allowPublicKeyRetrieval=true&useSSL=false";
        String username      = "root";
        String password      = "root-password";

        conn = DriverManager.getConnection(url, username, password);
    }

    // 插入更新表格
    public
    void Upsert(TableData table_data) throws SQLException
    {
        String                       table_name = table_data.table_name();
        ArrayList<String>            headers    = table_data.headers();
        ArrayList<ArrayList<String>> data       = table_data.data();

        for(var row_data : data)
        {
            // 构建 SQL 语句
            StringBuilder sql_builder = new StringBuilder();
            sql_builder.append("INSERT INTO ").append(table_name).append(" (");

            // 列名
            for(int i = 0; i < headers.size(); i++)
            {
                sql_builder.append(headers.get(i));
                if(i < headers.size() - 1) sql_builder.append(", ");
            }

            sql_builder.append(") VALUES (");

            // 占位符
            for(int i = 0; i < headers.size(); i++)
            {
                sql_builder.append("?");
                if(i < headers.size() - 1) sql_builder.append(", ");
            }

            sql_builder.append(") ON DUPLICATE KEY UPDATE ");

            // 更新部分
            for(int i = 0; i < headers.size(); i++)
            {
                sql_builder.append(headers.get(i)).append(" = VALUES(").append(headers.get(i)).append(")");
                if(i < headers.size() - 1) sql_builder.append(", ");
            }

            String sql_str = sql_builder.toString();
            IO.println(sql_str);

            try(var stmt = conn.prepareStatement(sql_str))
            {
                // 设置参数
                for(int i = 0; i < row_data.size(); i++)
                {
                    stmt.setString(i + 1, row_data.get(i));
                }

                // 执行更新
                stmt.executeUpdate();
            }
        }

    }
}
