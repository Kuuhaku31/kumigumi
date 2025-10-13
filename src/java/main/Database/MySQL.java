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
        String database_url  = "jdbc:mysql://localhost:3306/";
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

    public
    void Upsert(AnimeInfo ani_info) throws SQLException
    {
        // 更新逻辑
        String sql_str =
            """
            INSERT INTO anime
            (
               `ANI_ID`            ,
               `air_date`          ,
               `title`             ,
               `title_cn`          ,
               `aliases`           ,
               `episode_count`     ,
               `url_official_site` ,
               `url_cover`         ,
               `url_rss`           ,
               `rating_before`     ,
               `rating_after`      ,
               `remark`
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) -- 占位符，防止 SQL 注入
            ON DUPLICATE KEY UPDATE                     -- 如果主键冲突，则更新以下字段
            `ANI_ID`            = VALUES(ANI_ID),
            `air_date`          = VALUES(air_date),
            `title`             = VALUES(title),
            `title_cn`          = VALUES(title_cn),
            `aliases`           = VALUES(aliases),
            `episode_count`     = VALUES(episode_count),
            `url_official_site` = VALUES(url_official_site),
            `url_cover`         = VALUES(url_cover),
            `url_rss`           = VALUES(url_rss),
            `rating_before`     = VALUES(rating_before),
            `rating_after`      = VALUES(rating_after),
            `remark`            = VALUES(remark);
            """;

        // 创建预编译语句
        var stmt = conn.prepareStatement(sql_str);
        stmt.setInt(1, ani_info.ani_id);
        stmt.setObject(2, ani_info.air_date);
        stmt.setString(3, ani_info.title);
        stmt.setString(4, ani_info.title_cn);
        stmt.setString(5, ani_info.aliases);
        stmt.setInt(6, ani_info.episode_count);
        stmt.setString(7, ani_info.url_official_site);
        stmt.setString(8, ani_info.url_cover);
        stmt.setString(9, ani_info.url_rss);
        stmt.setInt(10, ani_info.pre_view_rating);
        stmt.setInt(11, ani_info.after_view_rating);
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
                 `EPI_ID`          ,
                 `ANI_ID`          ,
                 `air_date`        ,
                 `duration`        ,
                 `index`           ,
                 `title`           ,
                 `title_cn`        ,
                 `description`     ,
                 `status_download` ,
                 `status_view`     ,
                 `rating`          ,
                 `remark`
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
            `EPI_ID`          = VALUES(EPI_ID),
            `ANI_ID`          = VALUES(ANI_ID),
            `air_date`        = VALUES(air_date),
            `duration`        = VALUES(duration),
            `index`           = VALUES(`index`),
            `title`           = VALUES(title),
            `title_cn`        = VALUES(title_cn),
            `description`     = VALUES(description),
            `status_download` = VALUES(status_download),
            `status_view`     = VALUES(status_view),
            `rating`          = VALUES(rating),
            `remark`          = VALUES(remark);
            """;

        // 创建预编译语句
        var stmt = conn.prepareStatement(sql_str);
        stmt.setInt(1, episode_info.ep_id);
        stmt.setInt(2, episode_info.ani_id);
        stmt.setObject(3, episode_info.air_date);
        stmt.setInt(4, episode_info.duration.toSecondOfDay());
        stmt.setString(5, episode_info.index);
        stmt.setString(6, episode_info.title);
        stmt.setString(7, episode_info.title_cn);
        stmt.setString(8, episode_info.description);
        stmt.setString(9, episode_info.status_download.toString());
        stmt.setString(10, episode_info.status_view.toString());
        stmt.setInt(11, episode_info.rating);
        stmt.setString(12, episode_info.remark);

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
               `TOR_URL`         ,
               `ANI_ID`          ,
               `air_datetime`    ,
               `size`            ,
               `url_page`        ,
               `title`           ,
               `subtitle_group`  ,
               `description`     ,
               `status_download` ,
               `remark`
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
            `TOR_URL`           = VALUES(TOR_URL),
            `ANI_ID`            = VALUES(ANI_ID),
            `air_datetime`      = VALUES(air_datetime),
            `size`              = VALUES(size),
            `url_page`          = VALUES(url_page),
            `title`             = VALUES(title),
            `subtitle_group`    = VALUES(subtitle_group),
            `description`       = VALUES(description),
            `status_download`   = VALUES(status_download),
            `remark`            = VALUES(remark);
            """;

        // 创建预编译语句
        var stmt = conn.prepareStatement(sql_str);
        stmt.setString(1, torrent_info.tor_url);
        stmt.setInt(2, torrent_info.ani_id);
        stmt.setObject(3, torrent_info.air_datetime);
        stmt.setLong(4, torrent_info.size);
        stmt.setString(5, torrent_info.url_page);
        stmt.setString(6, torrent_info.title);
        stmt.setString(7, torrent_info.subtitle_group);
        stmt.setString(8, torrent_info.description);
        stmt.setString(9, torrent_info.status_download.toString());
        stmt.setString(10, torrent_info.remark);

        // 执行更新
        stmt.executeUpdate();
        stmt.close();
    }

    /**
     * 删除指定番组 ID 下，所有不在 exist_info_id 列表中的剧集信息。
     */
    public
    void DeleteInvalidEpisodeInfo(int ani_id, int[] exist_info_id) throws SQLException
    {
        if(exist_info_id == null || exist_info_id.length == 0)
        {
            // 若 exist_info_id 为空，则直接删除该番组下的所有剧集
            try(var stmt = conn.prepareStatement("DELETE FROM `episode` WHERE `ANI_ID` = ?"))
            {
                stmt.setInt(1, ani_id);
                stmt.executeUpdate();
            }
        }
        else // exist_info_id 非空
        {
            // 构建 SQL —— 直接使用 NOT IN 一步删除，不必手动循环查询
            StringBuilder sql = new StringBuilder("DELETE FROM `episode` WHERE `ANI_ID` = ? AND `EPI_ID` NOT IN (");
            sql.append("?,".repeat(exist_info_id.length));           // 为每个 exist_info_id 元素添加一个占位符
            sql.setCharAt(sql.length() - 1, ')');// 将最后一个逗号替换为右括号

            // 创建预编译语句并执行
            try(var stmt = conn.prepareStatement(sql.toString()))
            {
                // 第 1 个占位符是 ani_id
                stmt.setInt(1, ani_id);
                for(int i = 0; i < exist_info_id.length; i++) stmt.setInt(i + 2, exist_info_id[i]);
                stmt.executeUpdate();
            }
        }
    }

}
