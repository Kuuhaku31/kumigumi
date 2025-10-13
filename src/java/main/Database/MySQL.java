// MySQL.java

package Database;

import utils.AnimeInfo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public
class MySQL
{
    private final String table_name_anime = "anime";
    private final String table_name_episode = "episode";
    private final String table_name_torrent = "torrent";

    private Connection conn;

    public
    void Open() throws SQLException
    {
        String database_url = "jdbc:mysql://localhost:3306/";
        String database_name = "kumigumi-new";
        String url = database_url + database_name + "?allowPublicKeyRetrieval=true&useSSL=false";
        String username = "root";
        String password = "root-password";

        conn = DriverManager.getConnection(url, username, password);
    }

    public
    void Close() throws SQLException
    {
        if(conn != null && !conn.isClosed()) conn.close();
    }

    public
    void Upsert(AnimeInfo ani_info) throws SQLException
    {
        if(conn == null)
        {
            IO.println("数据库未连接，无法更新");
            return;
        }

        // 更新逻辑
        String sql_str = String.format(
            """
            INSERT INTO %s
            (
                bangumi_id,
                air_date,
                title,
                title_cn,
                aliases,
                episode_count,
                official_site_url,
                cover_url,
                pre_view_rating,
                after_view_rating,
                rss_url,
                remark
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) -- 占位符，防止 SQL 注入
            ON DUPLICATE KEY UPDATE                     -- 如果主键冲突，则更新以下字段
            title             = VALUES(title),
            air_date          = VALUES(air_date),
            title_cn          = VALUES(title_cn),
            aliases           = VALUES(aliases),
            episode_count     = VALUES(episode_count),
            official_site_url = VALUES(official_site_url),
            cover_url         = VALUES(cover_url),
            pre_view_rating   = VALUES(pre_view_rating),
            after_view_rating = VALUES(after_view_rating),
            rss_url           = VALUES(rss_url),
            remark            = VALUES(remark);""",
            table_name_anime
        );

        // 创建预编译语句
        var stmt = conn.prepareStatement(sql_str);
        stmt.setInt(1, ani_info.ani_id);
        stmt.setObject(2, ani_info.air_date);
        stmt.setString(3, ani_info.title);
        stmt.setString(4, ani_info.title_cn);
        stmt.setString(5, ani_info.aliases);
        stmt.setInt(6, ani_info.episode_count);
        stmt.setString(7, ani_info.official_site_url);
        stmt.setString(8, ani_info.cover_url);
        stmt.setInt(9, ani_info.pre_view_rating);
        stmt.setInt(10, ani_info.after_view_rating);
        stmt.setString(11, ani_info.rss_url);
        stmt.setString(12, ani_info.remark);

        // 执行更新
        stmt.executeUpdate();
        stmt.close();
    }

    public
    enum TableName
    {
        anime,
        episode,
        torrent
    }
}
