// MySQLDemo.java

package Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public
class MySQLDemo
{
    static
    Connection get_connection(String[] args) throws SQLException
    {
        if(args.length < 3)
        {
            IO.println("Error: 需要提供用户名、密码和数据库名作为参数");
            return null;
        }
        String username = args[0];
        String password = args[1];
        String dbName = args[2];
        String url = "jdbc:mysql://localhost:3306/" + dbName + "?useSSL=false&serverTimezone=UTC";
        return DriverManager.getConnection(url, username, password);
    }

    static
    void TestConnection(String[] args)
    {
        IO.println("测试MySQL连接");
        try(Connection conn = get_connection(args))
        {
            if(conn != null && !conn.isClosed()) IO.println("连接成功!");
            else IO.println("连接失败!");
        }
        catch(SQLException e)
        {
            IO.println("SQL异常: " + e.getMessage());
        }
    }

    static
    void PrintDatabaseList(String[] args)
    {
        IO.println("打印数据库列表");
        try(Connection conn = get_connection(args))
        {
            if(conn == null) return;
            var stmt = conn.createStatement();
            var rs = stmt.executeQuery("SHOW DATABASES");
            IO.println("数据库列表:");
            while(rs.next())
            {
                IO.println(" - " + rs.getString(1));
            }
            rs.close();
            stmt.close();
        }
        catch(SQLException e)
        {
            IO.println("SQL异常: " + e.getMessage());
        }
    }

    static
    void PrintTableList(String[] args)
    {
        IO.println("打印表列表");
        try(Connection conn = get_connection(args))
        {
            if(conn == null) return;
            var stmt = conn.createStatement();
            var rs = stmt.executeQuery("SHOW TABLES");
            IO.println("表列表:");
            while(rs.next())
            {
                IO.println(" - " + rs.getString(1));
            }
            rs.close();
            stmt.close();
        }
        catch(SQLException e)
        {
            IO.println("SQL异常: " + e.getMessage());
        }
    }

    static
    void PrintData(String[] args)
    {
        IO.println("打印数据");
        try(Connection conn = get_connection(args))
        {
            if(conn == null) return;

            String query = "SELECT * FROM users LIMIT 5";

            var stmt = conn.createStatement();
            try(var rs = stmt.executeQuery(query))
            {
                var meta = rs.getMetaData();
                int columnCount = meta.getColumnCount();

                // 打印列名
                for(int i = 1; i <= columnCount; i++)
                {
                    IO.print(meta.getColumnName(i) + "\t");
                }
                IO.println();

                // 打印行数据
                while(rs.next())
                {
                    for(int i = 1; i <= columnCount; i++)
                    {
                        IO.print(rs.getString(i) + "\t");
                    }
                    IO.println();
                }
            }
            stmt.close();
        }
        catch(SQLException e)
        {
            IO.println("SQL异常: " + e.getMessage());
        }
    }

    static
    void main(String[] args)
    {
        IO.println("MySQLDemo");

        TestConnection(args);
        PrintDatabaseList(args);
        PrintTableList(args);
        IO.println("================================");
        PrintData(args);
    }
}
