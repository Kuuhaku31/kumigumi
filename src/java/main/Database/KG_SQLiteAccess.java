package Database;


import utils.TableData.TableData;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public
class KG_SQLiteAccess
{
    private final List<String> ANIME_HEADERS   = List.of(
        "ANI_ID",
        "air_date",
        "title",
        "title_cn",
        "aliases",
        "description",
        "episode_count",
        "url_official_site",
        "url_cover",
        "url_rss",
        "rating_before",
        "rating_after",
        "remark"
    );
    private final List<String> EPISODE_HEADERS = List.of(
        "EPI_ID",
        "ANI_ID",
        "sort",
        "air_date",
        "duration",
        "ep",
        "title",
        "title_cn",
        "description",
        "rating",
        "status_download",
        "status_view",
        "remark"
    );
    private final List<String> TORRENT_HEADERS = List.of(new String[] {
        "TOR_URL",
        "ANI_ID",
        "air_datetime",
        "size",
        "url_page",
        "title",
        "subtitle_group",
        "description",
        "status_download",
        "remark"
    });

    private final List<String> ANIME_PRIMARY_KEY   = List.of(new String[] {"ANI_ID"});
    private final List<String> EPISODE_PRIMARY_KEY = List.of(new String[] {"EPI_ID"});
    private final List<String> TORRENT_PRIMARY_KEY = List.of(new String[] {"TOR_URL"});


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
        if(conn == null) return;

        // 准备表头
        List<String> primary_key   = null;
        List<String> target_header = null;
        switch(table_name)
        {
        case anime ->
        {
            primary_key   = ANIME_PRIMARY_KEY;
            target_header = ANIME_HEADERS;
        }
        case episode ->
        {
            primary_key   = EPISODE_PRIMARY_KEY;
            target_header = EPISODE_HEADERS;
        }
        case torrent ->
        {
            primary_key   = TORRENT_PRIMARY_KEY;
            target_header = TORRENT_HEADERS;
        }
        }

        // 确保包含
        List<String> upsert_header = new ArrayList<>();
        for(var header : data.GetHeaders()) if(target_header.contains(header)) upsert_header.add(header);


        // 构建通用 SQL 语句模板
        var columns      = String.join(", ", upsert_header.stream().map(h -> "`" + h + "`").toArray(String[]::new));
        var placeholders = String.join(", ", Collections.nCopies(upsert_header.size(), "?"));
        var primary_key_str = String.join(", ", primary_key
            .stream()
            .map(h -> "`" + h + "`")
            .toArray(String[]::new)
        );
        var update_part = String.join(", ", upsert_header.stream()
            .filter(h -> !h.equals("id"))
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


