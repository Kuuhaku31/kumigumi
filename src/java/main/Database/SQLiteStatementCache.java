package Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Cache of PreparedStatements for SQLiteAccess to use.
 */
class SQLiteStatementCache {
    final PreparedStatement psAniUpsert;
    final PreparedStatement psEpiUpsert;
    final PreparedStatement psTorUpsert;

    final PreparedStatement psAniFetch;
    final PreparedStatement psAniStore;
    final PreparedStatement psEpiFetch;
    final PreparedStatement psEpiStore;
    final PreparedStatement psTorFetch;
    final PreparedStatement psTorStore;

    final PreparedStatement psAniDelete;
    final PreparedStatement psEpiDelete;
    final PreparedStatement psTorDelete;

    SQLiteStatementCache(Connection conn) throws SQLException {
        psAniUpsert = conn.prepareStatement("""
                INSERT INTO anime (
                    ANI_ID
                ) VALUES (?)
                ON CONFLICT(ANI_ID) DO UPDATE SET
                    ANI_ID = excluded.ANI_ID;
                """);

        psEpiUpsert = conn.prepareStatement("""
                INSERT INTO episode (
                    EPI_ID,
                    ANI_ID
                ) VALUES (?, ?)
                ON CONFLICT(EPI_ID) DO UPDATE SET
                    EPI_ID = excluded.EPI_ID;
                """);

        psTorUpsert = conn.prepareStatement("""
                INSERT INTO torrent (
                    TOR_URL,
                    ANI_ID
                ) VALUES (?, ?)
                ON CONFLICT(TOR_URL) DO UPDATE SET
                    TOR_URL = excluded.TOR_URL;
                """);

        psAniFetch = conn.prepareStatement("""
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

        psAniStore = conn.prepareStatement("""
                UPDATE anime
                SET
                    url_rss         = ?,
                    rating_before   = ?,
                    rating_after    = ?,
                    remark          = ?
                WHERE ANI_ID = ?;
                """);

        psEpiFetch = conn.prepareStatement("""
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

        psEpiStore = conn.prepareStatement("""
                UPDATE episode
                SET
                    rating          = ?,
                    view_datetime   = ?,
                    status_download = ?,
                    status_view     = ?,
                    remark          = ?
                WHERE EPI_ID = ?;
                """);

        psTorFetch = conn.prepareStatement("""
                UPDATE torrent
                SET
                    air_datetime    = ?,
                    size            = ?,
                    url_page        = ?,
                    title           = ?,
                    subtitle_group  = ?,
                    description     = ?
                WHERE TOR_URL = ?;
                """);

        psTorStore = conn.prepareStatement("""
                UPDATE torrent
                SET
                    status_download = ?,
                    remark          = ?
                WHERE TOR_URL = ?;
                """);

        psAniDelete = conn.prepareStatement("DELETE FROM anime WHERE ANI_ID = ?;");
        psEpiDelete = conn.prepareStatement("DELETE FROM episode WHERE EPI_ID = ?;");
        psTorDelete = conn.prepareStatement("DELETE FROM torrent WHERE TOR_URL = ?;");
    }

    void close() throws SQLException {
        closeQuiet(psAniUpsert);
        closeQuiet(psEpiUpsert);
        closeQuiet(psTorUpsert);

        closeQuiet(psAniFetch);
        closeQuiet(psAniStore);
        closeQuiet(psEpiFetch);
        closeQuiet(psEpiStore);
        closeQuiet(psTorFetch);
        closeQuiet(psTorStore);

        closeQuiet(psAniDelete);
        closeQuiet(psEpiDelete);
        closeQuiet(psTorDelete);
    }

    private static void closeQuiet(PreparedStatement ps) throws SQLException {
        if (ps != null) {
            ps.close();
        }
    }
}
