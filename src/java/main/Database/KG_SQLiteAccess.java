package Database;


import utils.TableData.TableData;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static Database.DBStructure.ANIME_HEADERS;

public
class KG_SQLiteAccess
{
    private Connection conn = null;

    public
    void Open()
    {
        String url = "jdbc:sqlite:D:/db/st-sqlite/kumigumi.db"; // 若文件不存在会自动创建
        try { conn = DriverManager.getConnection(url); }
        catch(SQLException e) { System.err.println(e.getMessage()); }
    }

    public
    boolean isOpen()
    { return conn != null; }

    public
    void Close()
    {
        try { conn.close(); }
        catch(SQLException e) { System.err.println(e.getMessage()); }
        conn = null;
    }

    public
    void Upsert(TableName table_name, TableData data)
    {
        if(conn == null)
        {
            System.err.println("数据库未打开，无法执行 Upsert 操作");
            return;
        }

        // 准备表头
        DBStructure.Headers[] primary_key;
        DBStructure.Headers[] target_header;
        switch(table_name)
        {
        case anime ->
        {
            primary_key   = new DBStructure.Headers[] {DBStructure.Headers.ANI_ID};
            target_header = ANIME_HEADERS;
        }
        case episode ->
        {
            primary_key   = new DBStructure.Headers[] {DBStructure.Headers.EPI_ID};
            target_header = DBStructure.EPISODE_HEADERS;
        }
        case torrent ->
        {
            primary_key   = new DBStructure.Headers[] {DBStructure.Headers.TOR_URL};
            target_header = DBStructure.TORRENT_HEADERS;
        }
        default ->
        {
            System.err.println("未知的表名，无法执行 Upsert 操作");
            return;
        }
        }


        // 安全性检测
        List<DBStructure.Headers> upsert_header = new ArrayList<>();

        var data_headers = data.GetHeaders();
        for(var pk : primary_key) // 确保主键存在
            if(data.GetHeaderIndex(String.valueOf(pk)) == -1)
            {
                System.err.println("主键不完整，无法执行 Upsert 操作");
                return;
            }
        for(var header : data_headers) // 确保包含，仅更新白名单中的列
        {
            for(var th : target_header)
            {
                if(th.toString().equals(header))
                {
                    upsert_header.add(th);
                    break;
                }
            }
        }

        // 构建通用 SQL 语句模板
        var columns      = String.join(", ", upsert_header.stream().map(h -> "`" + h + "`").toArray(String[]::new));
        var placeholders = String.join(", ", Collections.nCopies(upsert_header.size(), "?"));
        var primary_key_str = String.join(", ", Arrays.stream(primary_key)
            .map(h -> "`" + h + "`")
            .toArray(String[]::new)
        );
        var update_part = String.join(", ", upsert_header.stream()
            .map(h -> "`" + h + "` = excluded." + h)
            .toArray(String[]::new)
        );


        // 插入数据
        var sql = "INSERT INTO `" + table_name + "` (" + columns + ")\n"
            + "VALUES (" + placeholders + ")\n"
            + "ON CONFLICT (" + primary_key_str + ")" + " DO UPDATE SET " + update_part;
        try(var stmt = conn.prepareStatement(sql))
        {
            // 启用事务
            conn.setAutoCommit(false);

            // 遍历所有行数据，加入批量
            var rows = data.GetData();
            for(var row_data : rows)
            {
                for(int i = 0; i < upsert_header.size(); i++) stmt.setString(i + 1, row_data[i]);
                stmt.addBatch();
            }

            // 执行批处理
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

    public
    enum TableName
    {
        anime,
        episode,
        torrent;

        public static
        TableName Get(String value)
        {
            return switch(value)
            {
                case "anime" -> anime;
                case "episode" -> episode;
                case "torrent" -> torrent;
                default -> null;
            };
        }
    }
}


