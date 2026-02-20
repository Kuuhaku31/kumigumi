package Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * SQLite 语句缓存，预编译 SQL 语句以提高性能，并提供统一的接口来执行这些语句。
 * 该类负责管理所有与 anime、episode 和 torrent 相关的 SQL 语句，包括插入、更新和删除操作。
 * 通过使用 PreparedStatement，可以防止 SQL 注入攻击，并且在执行相同的 SQL 语句时提高效率。
 */
class SQLiteStatementCache {
    final PreparedStatement psAniUpsert;    // 插入或更新 anime 项目
    final PreparedStatement psEpiUpsert;    // 插入或更新 episode 项目
    final PreparedStatement psTorUpsert;    // 插入或更新 torrent 项目
    final PreparedStatement psAniTorUpsert; // 插入或更新 ani-tor 项目

    final PreparedStatement psAniFetch;    // 获取 anime 项目
    final PreparedStatement psAniStore;    // 存储 anime 项目
    final PreparedStatement psEpiFetch;    // 获取 episode 项目
    final PreparedStatement psEpiStore;    // 存储 episode 项目
    final PreparedStatement psTorFetch;    // 获取 torrent 项目
    final PreparedStatement psTorStore;    // 存储 torrent 项目
    final PreparedStatement psAniTorFetch; // 获取 ani-tor 项目
    final PreparedStatement psAniTorStore; // 存储 ani-tor 项目

    final PreparedStatement psAniDelete;    // 删除 anime 项目
    final PreparedStatement psEpiDelete;    // 删除 episode 项目
    final PreparedStatement psTorDelete;    // 删除 torrent 项目
    final PreparedStatement psAniTorDelete; // 删除 ani-tor 项目

    /**
     * 构造函数，初始化所有预编译的 SQL 语句。
     * @param conn 数据库连接对象
     * @throws SQLException 如果初始化过程中发生 SQL 错误
     */
    SQLiteStatementCache(Connection conn) throws SQLException {

        // 插入或更新
        psAniUpsert = conn.prepareStatement(
            """
            INSERT INTO anime (
                ANI_ID
            ) VALUES (?)
            ON CONFLICT(ANI_ID) DO UPDATE SET
                ANI_ID = excluded.ANI_ID;
            """);
        psEpiUpsert = conn.prepareStatement(
            """
            INSERT INTO episode (
                EPI_ID,
                ANI_ID
            ) VALUES (?, ?)
            ON CONFLICT(EPI_ID) DO UPDATE SET
                EPI_ID = excluded.EPI_ID;
            """);
        psTorUpsert = conn.prepareStatement(
            """
            INSERT INTO torrent (
                TOR_HASH
            ) VALUES (?)
            ON CONFLICT(TOR_HASH) DO UPDATE SET
                TOR_HASH = excluded.TOR_HASH;
            """);
        psAniTorUpsert = conn.prepareStatement(
            """
            INSERT INTO ani_tor (
                ANI_ID,
                TOR_HASH
            ) VALUES (?, ?)
            ON CONFLICT(ANI_ID, TOR_HASH) DO UPDATE SET
                ANI_ID = excluded.ANI_ID,
                TOR_HASH = excluded.TOR_HASH;
            """);


        // 更新

        // anime
        psAniFetch = conn.prepareStatement(
            """
            UPDATE anime
            SET
                air_date            = ?,
                title               = ?,
                title_cn            = ?,
                aliases             = ?,
                description         = ?,
                episode_count       = ?,
                url_official_site   = ?,
                url_cover           = ?
            WHERE ANI_ID = ?;
            """);
        psAniStore = conn.prepareStatement(
            """
            UPDATE anime
            SET
                url_rss         = ?,
                rating_before   = ?,
                rating_after    = ?,
                remark          = ?
            WHERE ANI_ID = ?;
            """);

        // episode
        psEpiFetch = conn.prepareStatement(
            """
            UPDATE episode
            SET
                ep          = ?,
                sort        = ?,
                air_date    = ?,
                duration    = ?,
                title       = ?,
                title_cn    = ?,
                description = ?
            WHERE EPI_ID = ?;
            """);
        psEpiStore = conn.prepareStatement(
            """
            UPDATE episode
            SET
                rating          = ?,
                view_datetime   = ?,
                status_download = ?,
                status_view     = ?,
                remark          = ?
            WHERE EPI_ID = ?;
            """);

        // torrent
        psTorFetch = conn.prepareStatement(
            """
            UPDATE torrent
            SET
                file_name    = ?,
                file_size    = ?,
                torrent_file = ?
            WHERE TOR_HASH = ?;
            """);
        psTorStore = conn.prepareStatement(
            """
            UPDATE torrent
            SET
                remark = ?
            WHERE TOR_HASH = ?;
            """);

        // anime-torrent 关联项
        psAniTorFetch = conn.prepareStatement(
            """
            UPDATE ani_tor
            SET
                air_datetime    = ?,
                url_download    = ?,
                url_page        = ?,
                title           = ?,
                subtitle_group  = ?,
                description     = ?
            WHERE ANI_ID = ? AND TOR_HASH = ?;
            """);
        psAniTorStore = conn.prepareStatement(
            """
            UPDATE ani_tor
            SET
                status_download = ?,
                remark          = ?
            WHERE ANI_ID = ? AND TOR_HASH = ?;
            """);

        // 删除
        psAniDelete    = conn.prepareStatement("DELETE FROM anime WHERE ANI_ID = ?;");
        psEpiDelete    = conn.prepareStatement("DELETE FROM episode WHERE EPI_ID = ?;");
        psTorDelete    = conn.prepareStatement("DELETE FROM torrent WHERE TOR_HASH = ?;");
        psAniTorDelete = conn.prepareStatement("DELETE FROM ani_tor WHERE ANI_ID = ? AND TOR_HASH = ?;");
    }

    /**
     * 关闭所有预编译的 SQL 语句，释放数据库资源。
     * @throws SQLException 如果关闭过程中发生 SQL 错误
     */
    void close() throws SQLException {
        closePs(psAniUpsert);
        closePs(psEpiUpsert);
        closePs(psTorUpsert);
        closePs(psAniTorUpsert);

        closePs(psAniFetch);
        closePs(psAniStore);
        closePs(psEpiFetch);
        closePs(psEpiStore);
        closePs(psTorFetch);
        closePs(psTorStore);
        closePs(psAniTorFetch);
        closePs(psAniTorStore);

        closePs(psAniDelete);
        closePs(psEpiDelete);
        closePs(psTorDelete);
        closePs(psAniTorDelete);
    }

    /**
     * 安静地关闭 PreparedStatement，忽略任何 SQL 异常。
     * @param ps 要关闭的 PreparedStatement 对象
     * @throws SQLException 如果关闭过程中发生 SQL 错误
     */
    private static void closePs(PreparedStatement ps) throws SQLException {
        if(ps != null) {
            ps.close();
        }
    }
}
