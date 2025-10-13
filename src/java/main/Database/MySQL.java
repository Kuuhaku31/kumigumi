// MySQL.java

package Database;

import utils.Info.AnimeInfo;
import utils.Info.EpisodeInfo;
import utils.Info.TorrentInfo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public
class MySQL
{
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
        // 更新逻辑
        String sql_str =
            """
            INSERT INTO anime
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
            remark            = VALUES(remark);
            """;

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
    void Upsert(EpisodeInfo episode_info) throws SQLException
    {
        // 更新逻辑
        String sql_str =
            """
            INSERT INTO episode
            (
                 episode_id,
                 anime_id,
                 air_date,
                 episode_index,
                 episode_title,
                 episode_title_cn,
                 episode_duration,
                 episode_rating,
                 episode_download_status,
                 episode_view_status,
                 remark
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
            anime_id                = VALUES(anime_id),
            air_date                = VALUES(air_date),
            episode_index           = VALUES(episode_index),
            episode_title           = VALUES(episode_title),
            episode_title_cn        = VALUES(episode_title_cn),
            episode_duration        = VALUES(episode_duration),
            episode_rating          = VALUES(episode_rating),
            episode_download_status = VALUES(episode_download_status),
            episode_view_status     = VALUES(episode_view_status),
            remark                  = VALUES(remark);
            """;

        // 创建预编译语句
        var stmt = conn.prepareStatement(sql_str);
        stmt.setInt(1, episode_info.ep_id);
        stmt.setInt(2, episode_info.ani_id);
        stmt.setObject(3, episode_info.air_date);
        stmt.setString(4, episode_info.index);
        stmt.setString(5, episode_info.title);
        stmt.setString(6, episode_info.title_cn);
        stmt.setObject(7, episode_info.duration.toSecondOfDay());
        stmt.setInt(8, episode_info.rating);
        stmt.setString(9, episode_info.download_status);
        stmt.setString(10, episode_info.view_status);
        stmt.setString(11, episode_info.remark);

        // 执行更新
        stmt.executeUpdate();
        stmt.close();
    }

    public
    void Upsert(TorrentInfo torrent_info) throws SQLException
    {
        // 更新逻辑
        String sql_str =
            """
            INSERT INTO torrent
            (
                torrent_download_url,
                anime_id,
                air_date,
                page_url,
                subtitle_group,
                title,
                description,
                size_bytes,
                download_status,
                remark
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
            anime_id       = VALUES(anime_id),
            air_date       = VALUES(air_date),
            page_url       = VALUES(page_url),
            subtitle_group = VALUES(subtitle_group),
            title          = VALUES(title),
            description    = VALUES(description),
            size_bytes     = VALUES(size_bytes),
            download_status= VALUES(download_status),
            remark         = VALUES(remark);
            """;

        // 创建预编译语句
        var stmt = conn.prepareStatement(sql_str);
        stmt.setString(1, torrent_info.torrent_url);
        stmt.setInt(2, torrent_info.ani_id);
        stmt.setObject(3, torrent_info.air_date_time);
        stmt.setString(4, torrent_info.page_url);
        stmt.setString(5, torrent_info.subtitle_group);
        stmt.setString(6, torrent_info.title);
        stmt.setString(7, torrent_info.description);
        stmt.setLong(8, torrent_info.size);
        stmt.setString(9, torrent_info.download_status);
        stmt.setString(10, torrent_info.remark);

        // 执行更新
        stmt.executeUpdate();
        stmt.close();
    }
}
