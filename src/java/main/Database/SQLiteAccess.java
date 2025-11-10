// SQLiteAccess.java


package Database;// Database.SQLiteAccess.java

import utils.TableData;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public
class SQLiteAccess implements DBA
{
    private List<TableInfo> table_info_list = null;
    private Connection      conn            = null;


    @Override
    public
    void Open()
    {
        String url = "jdbc:sqlite:D:/db/st-sqlite/kumigumi.db"; // 若文件不存在会自动创建
        try { conn = DriverManager.getConnection(url); }
        catch(SQLException e) { e.fillInStackTrace(); }

        table_info_list = new ArrayList<>();

        try
        {
            // 获取所有表
            var meta   = conn.getMetaData();
            var tables = meta.getTables(null, null, "%", new String[] {"TABLE"});
            while(tables.next())
            {
                // 获取表名
                var table_name = tables.getString("TABLE_NAME");
                var info       = new TableInfo(table_name);

                // 获取该表的键
                var rs = conn.createStatement().executeQuery("PRAGMA table_info(" + table_name + ");");
                while(rs.next())
                {
                    var column = rs.getString("name");

                    info.headers.add(column);
                    if(rs.getInt("pk") > 0) info.primary_key.add(column); // 如果是主键
                }

                // 添加
                table_info_list.add(info);
            }
        }
        catch(SQLException e) { System.err.println(e.getMessage()); }
    }

    @Override
    public
    boolean isOpen()
    { return conn != null; }

    @Override
    public
    void Close()
    { conn = null; }

    @Override
    public
    void Upsert(String table_name, TableData data)
    {
        // 准备表头
        if(conn == null) return;
        String       name          = null;
        List<String> upsert_header = null;
        List<String> primary_key   = null;
        for(var info : table_info_list)
            if(info.name.equals(table_name))
            {
                name          = info.name;
                primary_key   = info.primary_key;
                upsert_header = new ArrayList<>(); // 确保包含
                for(var header : data.GetHeaders()) if(info.headers.contains(header)) upsert_header.add(header);
                break;
            }
        if(name == null) return;

        // 构建通用 SQL 语句模板
        var columns      = String.join(", ", upsert_header.stream().map(h -> "`" + h + "`").toArray(String[]::new));
        var placeholders = String.join(", ", Collections.nCopies(upsert_header.size(), "?"));
        var update_part = String.join(", ", upsert_header.stream()
            .filter(h -> !h.equals("id"))
            .map(h -> "`" + h + "` = excluded." + h)
            .toArray(String[]::new)
        );
        var primary_key_str = String.join(", ", primary_key
            .stream()
            .map(h -> "`" + h + "`")
            .toArray(String[]::new)
        );

        // 插入数据
        var sql = "INSERT INTO `" + name + "` (" + columns + ")\n"
            + "VALUES (" + placeholders + ")\n"
            + "ON CONFLICT (" + primary_key_str + ")" + " DO UPDATE SET " + update_part;
        try(var stmt = conn.prepareStatement(sql))
        {
            conn.setAutoCommit(false); // 启用事务

            // 2️⃣ 遍历所有行数据
            var rows = data.GetData();
            for(var row_data : rows)
            {
                for(int i = 0; i < upsert_header.size(); i++) stmt.setString(i + 1, row_data[i]);
                stmt.addBatch(); // 加入批量
            }

            // 3️⃣ 执行批处理
            stmt.executeBatch();
            conn.commit();
        }
        catch(SQLException e)
        {
            System.err.println(e.getMessage());
            try { conn.rollback(); }
            catch(SQLException e1) { System.err.println(e1.getMessage()); }
        }
        finally
        {
            try { conn.setAutoCommit(true); }
            catch(SQLException e) { System.err.println(e.getMessage()); }
        }
    }

    void main()
    {
        Open();
        System.out.println("Open");
        String query = "SELECT * FROM Customer";
        try(Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query))
        {
            while(rs.next())
            {
                System.out.println(rs.getInt("ID") + " | " +
                    rs.getString("name") + " | " +
                    rs.getString("address") + " | " +
                    rs.getString("phone")
                );
            }
        }
        catch(SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    private static
    class TableInfo
    {
        String       name;
        List<String> headers     = new ArrayList<>();
        List<String> primary_key = new ArrayList<>();

        public
        TableInfo(String name) { this.name = name; }
    }
}


