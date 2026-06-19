package Database;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Set;

import Info.AnimeInfo;
import Info.BaseInfo;
import Info.EpisodeInfo;
import Info.EpisodeRecordInfo;
import Info.RSSInfo;
import Info.TorrentInfo;
import Info.TorrentPageInfo;


final class Transactions {

    private Transactions() {}

    static void replaceAniIds(Connection connect, Set<Integer> newAniIDs) throws SQLException {

        final String DELETE_REQUIRED_ANIME_IDS = "DELETE FROM required_anime_id;";
        final String INSERT_REQUIRED_ANIME_ID  = "INSERT INTO required_anime_id (ANI_ID) VALUES (?);";

        var prev_auto = connect.getAutoCommit();
        connect.setAutoCommit(false);
        try(
            var delete_statement = connect.prepareStatement(DELETE_REQUIRED_ANIME_IDS);
            var insert_statement = connect.prepareStatement(INSERT_REQUIRED_ANIME_ID)
        ) {
            delete_statement.executeUpdate();
            for(var i : newAniIDs) {
                if(i == null) continue;
                try {
                    insert_statement.setInt(1, i);
                    insert_statement.executeUpdate();
                } catch(SQLException e) {
                    System.err.println("数据库写入失败: required_anime_id");
                    System.err.println("错误信息: " + e.getMessage());
                    System.err.println("问题数据项: ANI_ID=" + i);
                    throw e;
                }
            }
            connect.commit();
        }
        catch(SQLException | RuntimeException e) { connect.rollback(); throw e; }
        finally { connect.setAutoCommit(prev_auto); }
    }

    static void upsertInfoAnimeInfo(
        Connection     connect,
        Set<AnimeInfo> info_set
    ) throws SQLException {

        final String UPSERT_SQL =
        """
        INSERT INTO anime (
            ANI_ID, air_date, title, title_cn, aliases, description,
            episode_count, url_official_site, url_cover, update_datetime
        )
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        ON CONFLICT(ANI_ID) DO UPDATE SET
            air_date          = excluded.air_date,
            title             = excluded.title,
            title_cn          = excluded.title_cn,
            aliases           = excluded.aliases,
            description       = excluded.description,
            episode_count     = excluded.episode_count,
            url_official_site = excluded.url_official_site,
            url_cover         = excluded.url_cover,
            update_datetime   = excluded.update_datetime;
        """;

        try(var ps = connect.prepareStatement(UPSERT_SQL)) {
            var index = 0;
            for(var info : info_set) {
                index++;
                try {
                    DatabaseUtils.safeSetInt            (ps,  1, info.ANI_ID           );
                    DatabaseUtils.safeSetDate           (ps,  2, info.air_date         );
                    DatabaseUtils.safeSetString         (ps,  3, info.title            );
                    DatabaseUtils.safeSetString         (ps,  4, info.title_cn         );
                    DatabaseUtils.safeSetString         (ps,  5, info.aliases          );
                    DatabaseUtils.safeSetString         (ps,  6, info.description      );
                    DatabaseUtils.safeSetInt            (ps,  7, info.episode_count    );
                    DatabaseUtils.safeSetString         (ps,  8, info.url_official_site);
                    DatabaseUtils.safeSetString         (ps,  9, info.url_cover        );
                    DatabaseUtils.safeSetOffsetDateTime (ps, 10, info.update_datetime  );
                    ps.executeUpdate();
                } catch(SQLException e) {
                    print_info_failure(index, info, e);
                    throw e;
                }
            }
        }
    }

    static void upsertInfoEpisodeInfo(
        Connection      connect,
        Set<EpisodeInfo> info_set
    ) throws SQLException {

        final String UPSERT_SQL =
        """
        INSERT INTO episode (
            EPI_ID, ANI_ID, ep, sort, air_date, duration,
            title, title_cn, description, update_datetime
        )
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        ON CONFLICT(EPI_ID) DO UPDATE SET
            ANI_ID          = excluded.ANI_ID,
            ep              = excluded.ep,
            sort            = excluded.sort,
            air_date        = excluded.air_date,
            duration        = excluded.duration,
            title           = excluded.title,
            title_cn        = excluded.title_cn,
            description     = excluded.description,
            update_datetime = excluded.update_datetime;
        """;

        try(var ps = connect.prepareStatement(UPSERT_SQL)) {
            var index = 0;
            for(var info : info_set) {
                index++;
                try {
                    DatabaseUtils.safeSetInt            (ps,  1, info.EPI_ID         );
                    DatabaseUtils.safeSetInt            (ps,  2, info.ANI_ID         );
                    DatabaseUtils.safeSetInt            (ps,  3, info.ep             );
                    DatabaseUtils.safeSetDouble         (ps,  4, info.sort           );
                    DatabaseUtils.safeSetDate           (ps,  5, info.air_date       );
                    DatabaseUtils.safeSetInt            (ps,  6, info.duration       );
                    DatabaseUtils.safeSetString         (ps,  7, info.title          );
                    DatabaseUtils.safeSetString         (ps,  8, info.title_cn       );
                    DatabaseUtils.safeSetString         (ps,  9, info.description    );
                    DatabaseUtils.safeSetOffsetDateTime (ps, 10, info.update_datetime);
                    ps.executeUpdate();
                } catch(SQLException e) {
                    print_info_failure(index, info, e);
                    throw e;
                }
            }
        }
    }

    static void upsertInfoEpisodeRecordInfo(
        Connection            connect,
        Set<EpisodeRecordInfo> info_set
    ) throws SQLException {

        final String UPSERT_SQL =
        """
        INSERT INTO episode_record (EPI_ID, view_datetime, rating, comment)
        VALUES (?, ?, ?, ?)
        ON CONFLICT(EPI_ID, view_datetime) DO UPDATE SET
            rating  = excluded.rating,
            comment = excluded.comment;
        """;

        try(var ps = connect.prepareStatement(UPSERT_SQL)) {
            var index = 0;
            for(var info : info_set) {
                index++;
                try {
                    DatabaseUtils.safeSetInt            (ps, 1, info.EPI_ID       );
                    DatabaseUtils.safeSetOffsetDateTime (ps, 2, info.view_datetime);
                    DatabaseUtils.safeSetInt            (ps, 3, info.rating       );
                    DatabaseUtils.safeSetString         (ps, 4, info.comment      );
                    ps.executeUpdate();
                } catch(SQLException e) {
                    print_info_failure(index, info, e);
                    throw e;
                }
            }
        }
    }

    static void upsertInfoRSSInfo(
        Connection   connect,
        Set<RSSInfo> info_set
    ) throws SQLException {

        final String UPSERT_SQL =
        """
        INSERT INTO rss (URL_RSS, ANI_ID)
        VALUES (?, ?)
        ON CONFLICT(URL_RSS) DO UPDATE SET
            ANI_ID = excluded.ANI_ID;
        """;

        try(var ps = connect.prepareStatement(UPSERT_SQL)) {
            var index = 0;
            for(var info : info_set) {
                index++;
                try {
                    DatabaseUtils.safeSetString (ps, 1, info.URL_RSS);
                    DatabaseUtils.safeSetInt    (ps, 2, info.ANI_ID );
                    ps.executeUpdate();
                } catch(SQLException e) {
                    print_info_failure(index, info, e);
                    throw e;
                }
            }
        }
    }

    static void upsertInfoTorrentPageInfo(
        Connection           connect,
        Set<TorrentPageInfo> info_set
    ) throws SQLException {

        final String UPSERT_SQL =
        """
        INSERT INTO torrent_page (
            URL_RSS, TOR_HASH, air_datetime, url_download, url_page,
            title, subtitle_group, description, update_datetime
        )
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        ON CONFLICT(URL_RSS, TOR_HASH) DO UPDATE SET
            air_datetime    = excluded.air_datetime,
            url_download    = excluded.url_download,
            url_page        = excluded.url_page,
            title           = excluded.title,
            subtitle_group  = excluded.subtitle_group,
            description     = excluded.description,
            update_datetime = excluded.update_datetime;
        """;

        try(var ps = connect.prepareStatement(UPSERT_SQL)) {
            var index = 0;
            for(var info : info_set) {
                index++;
                try {
                    DatabaseUtils.safeSetString         (ps, 1, info.URL_RSS        );
                    DatabaseUtils.safeSetString         (ps, 2, info.TOR_HASH       );
                    DatabaseUtils.safeSetOffsetDateTime (ps, 3, info.air_datetime   );
                    DatabaseUtils.safeSetString         (ps, 4, info.url_download   );
                    DatabaseUtils.safeSetString         (ps, 5, info.url_page       );
                    DatabaseUtils.safeSetString         (ps, 6, info.title          );
                    DatabaseUtils.safeSetString         (ps, 7, info.subtitle_group );
                    DatabaseUtils.safeSetString         (ps, 8, info.description    );
                    DatabaseUtils.safeSetOffsetDateTime (ps, 9, info.update_datetime);
                    ps.executeUpdate();
                } catch(SQLException e) {
                    print_info_failure(index, info, e);
                    throw e;
                }
            }
        }
    }

    static void upsertInfoTorrentInfo(
        Connection       connect,
        Set<TorrentInfo> info_set
    ) throws SQLException {

        final String UPSERT_SQL =
        """
        INSERT INTO torrent (TOR_HASH, file_name, file_size, torrent_file)
        VALUES (?, ?, ?, ?)
        ON CONFLICT(TOR_HASH) DO UPDATE SET
            file_name    = excluded.file_name,
            file_size    = excluded.file_size,
            torrent_file = excluded.torrent_file;
        """;

        try(var ps = connect.prepareStatement(UPSERT_SQL)) {
            var index = 0;
            for(var info : info_set) {
                index++;
                try {
                    DatabaseUtils.safeSetString (ps, 1, info.TOR_HASH    );
                    DatabaseUtils.safeSetString (ps, 2, info.file_name   );
                    DatabaseUtils.safeSetLong   (ps, 3, info.file_size   );
                    DatabaseUtils.safeSetBytes  (ps, 4, info.torrent_file);
                    ps.executeUpdate();
                } catch(SQLException e) {
                    print_info_failure(index, info, e);
                    throw e;
                }
            }
        }
    }

    private static void print_info_failure(int index, BaseInfo info, SQLException error) {
        System.err.println("数据库写入失败");
        System.err.println("错误信息: " + error.getMessage());
        var type_name = info == null ? "null" : info.getClass().getSimpleName();
        System.err.println("问题数据项 #" + index + ": " + type_name);
        if(info == null) {
            System.err.println("null");
            return;
        }
        try { System.err.println(info.toPrintString("", false)); }
        catch(RuntimeException _) { System.err.println(info); }
    }
}
