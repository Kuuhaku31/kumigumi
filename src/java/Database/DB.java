// DB.java

package Database;

import java.sql.*;
import java.util.List;
import java.util.Map;

// 实现accdb数据库相关操作
public
class DB
{
    public String accdb_file_path = "";
    public String table_name = "";

    public
    DB(String accdb_file_path, String table_name)
    {
        this.accdb_file_path = accdb_file_path;
        this.table_name = table_name;
    }

    // 参数: List<Map<String,String>> rows: 每一行数据是一个Map, key是列名, value是对应的值
    // 逻辑:
    //     遍历List: 对于每个Map: 先检查Map是否存在主键
    //         如果不存在主键则添加到failedRows中，跳过该行
    //         如果存在主键，则检查数据库中是否存在该主键对应的记录
    //             如果存在，则更新该记录
    //             如果不存在，则插入该记录
    //     如果插入或更新失败，则将该行数据添加到failedRows中
    //     返回failedRows
    // 返回值: List<Map<String,String>> failedRows: 插入失败的行
    public static
    List<Map<String, String>> UpdateDatabase(String accdb_file_path, String table_name, List<Map<String, String>> data)
    {
        return null;
    }

    public
    void Test()
    {
        String url = "jdbc:ucanaccess://" + accdb_file_path;  // 数据库文件路径
        try(Connection conn = DriverManager.getConnection(url))
        {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM" + table_name);

            while(rs.next())
            {
                System.out.println(rs.getInt("ID") + " - " + rs.getString("Name"));
            }
        }
        catch(SQLException e)
        {
            e.printStackTrace();
        }

    }

    public
    void CreateTable()
    {
        String url = "jdbc:ucanaccess://" + accdb_file_path;  // 数据库文件路径
        try(Connection conn = DriverManager.getConnection(url))
        {
            Statement stmt = conn.createStatement();
            String sql = "CREATE TABLE " + table_name + " (ID INT PRIMARY KEY, Name TEXT)";
            stmt.executeUpdate(sql);
        }
        catch(SQLException e)
        {
            e.printStackTrace();
        }
    }
}
