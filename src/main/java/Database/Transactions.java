package Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
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
                insert_statement.setInt(1, i);
                insert_statement.addBatch();
            }
            insert_statement.executeBatch();
            connect.commit();
        }
        catch(SQLException | RuntimeException e) { connect.rollback(); throw e; }
        finally { connect.setAutoCommit(prev_auto); }
    }

    static void upsertInfoAnimeInfo(
        Connection     connect,
        Set<AnimeInfo> info_set,
        int            chunk_size
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

        final ParamSetter setter = (ps, info) -> {
            var animeInfo = (AnimeInfo) info;
            DatabaseUtils.safeSetInt            (ps,  1, animeInfo.ANI_ID           );
            DatabaseUtils.safeSetDate           (ps,  2, animeInfo.air_date         );
            DatabaseUtils.safeSetString         (ps,  3, animeInfo.title            );
            DatabaseUtils.safeSetString         (ps,  4, animeInfo.title_cn         );
            DatabaseUtils.safeSetString         (ps,  5, animeInfo.aliases          );
            DatabaseUtils.safeSetString         (ps,  6, animeInfo.description      );
            DatabaseUtils.safeSetInt            (ps,  7, animeInfo.episode_count    );
            DatabaseUtils.safeSetString         (ps,  8, animeInfo.url_official_site);
            DatabaseUtils.safeSetString         (ps,  9, animeInfo.url_cover        );
            DatabaseUtils.safeSetOffsetDateTime (ps, 10, animeInfo.update_datetime  );
        };

        execute_batch(connect, info_set, UPSERT_SQL, setter, chunk_size);
    }

    static void upsertInfoEpisodeInfo(
        Connection      connect,
        Set<EpisodeInfo> info_set,
        int             chunk_size
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

        final ParamSetter setter = (ps, info) -> {
            var episodeInfo = (EpisodeInfo) info;
            DatabaseUtils.safeSetInt            (ps,  1, episodeInfo.EPI_ID         );
            DatabaseUtils.safeSetInt            (ps,  2, episodeInfo.ANI_ID         );
            DatabaseUtils.safeSetInt            (ps,  3, episodeInfo.ep             );
            DatabaseUtils.safeSetDouble         (ps,  4, episodeInfo.sort           );
            DatabaseUtils.safeSetDate           (ps,  5, episodeInfo.air_date       );
            DatabaseUtils.safeSetInt            (ps,  6, episodeInfo.duration       );
            DatabaseUtils.safeSetString         (ps,  7, episodeInfo.title          );
            DatabaseUtils.safeSetString         (ps,  8, episodeInfo.title_cn       );
            DatabaseUtils.safeSetString         (ps,  9, episodeInfo.description    );
            DatabaseUtils.safeSetOffsetDateTime (ps, 10, episodeInfo.update_datetime);
        };

        execute_batch(connect, info_set, UPSERT_SQL, setter, chunk_size);
    }

    static void upsertInfoEpisodeRecordInfo(
        Connection            connect,
        Set<EpisodeRecordInfo> info_set,
        int                   chunk_size
    ) throws SQLException {

        final String UPSERT_SQL =
        """
        INSERT INTO episode_record (EPI_ID, view_datetime, rating, comment)
        VALUES (?, ?, ?, ?)
        ON CONFLICT(EPI_ID, view_datetime) DO UPDATE SET
            rating  = excluded.rating,
            comment = excluded.comment;
        """;

        final ParamSetter setter = (ps, info) -> {
            var recordInfo = (EpisodeRecordInfo) info;
            DatabaseUtils.safeSetInt            (ps, 1, recordInfo.EPI_ID       );
            DatabaseUtils.safeSetOffsetDateTime (ps, 2, recordInfo.view_datetime);
            DatabaseUtils.safeSetInt            (ps, 3, recordInfo.rating       );
            DatabaseUtils.safeSetString         (ps, 4, recordInfo.comment      );
        };

        execute_batch(connect, info_set, UPSERT_SQL, setter, chunk_size);
    }

    static void upsertInfoRSSInfo(
        Connection   connect,
        Set<RSSInfo> info_set,
        int          chunk_size
    ) throws SQLException {

        final String UPSERT_SQL =
        """
        INSERT INTO rss (URL_RSS, ANI_ID)
        VALUES (?, ?)
        ON CONFLICT(URL_RSS) DO UPDATE SET
            ANI_ID = excluded.ANI_ID;
        """;

        final ParamSetter setter = (ps, info) -> {
            var rssInfo = (RSSInfo) info;
            DatabaseUtils.safeSetString (ps, 1, rssInfo.URL_RSS);
            DatabaseUtils.safeSetInt    (ps, 2, rssInfo.ANI_ID );
        };

        execute_batch(connect, info_set, UPSERT_SQL, setter, chunk_size);
    }

    static void upsertInfoTorrentPageInfo(
        Connection           connect,
        Set<TorrentPageInfo> info_set,
        int                  chunk_size
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

        final ParamSetter setter = (ps, info) -> {
            var pageInfo = (TorrentPageInfo) info;
            DatabaseUtils.safeSetString         (ps, 1, pageInfo.URL_RSS        );
            DatabaseUtils.safeSetString         (ps, 2, pageInfo.TOR_HASH       );
            DatabaseUtils.safeSetOffsetDateTime (ps, 3, pageInfo.air_datetime   );
            DatabaseUtils.safeSetString         (ps, 4, pageInfo.url_download   );
            DatabaseUtils.safeSetString         (ps, 5, pageInfo.url_page       );
            DatabaseUtils.safeSetString         (ps, 6, pageInfo.title          );
            DatabaseUtils.safeSetString         (ps, 7, pageInfo.subtitle_group );
            DatabaseUtils.safeSetString         (ps, 8, pageInfo.description    );
            DatabaseUtils.safeSetOffsetDateTime (ps, 9, pageInfo.update_datetime);
        };

        execute_batch(connect, info_set, UPSERT_SQL, setter, chunk_size);
    }

    static void upsertInfoTorrentInfo(
        Connection       connect,
        Set<TorrentInfo> info_set,
        int              chunk_size
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

        final ParamSetter setter = (ps, info) -> {
            var torInfo = (TorrentInfo) info;
            DatabaseUtils.safeSetString (ps, 1, torInfo.TOR_HASH    );
            DatabaseUtils.safeSetString (ps, 2, torInfo.file_name   );
            DatabaseUtils.safeSetLong   (ps, 3, torInfo.file_size   );
            DatabaseUtils.safeSetBytes  (ps, 4, torInfo.torrent_file);
        };

        execute_batch(connect, info_set, UPSERT_SQL, setter, chunk_size);
    }

    @FunctionalInterface
    private interface ParamSetter {
        void set(PreparedStatement ps, BaseInfo info) throws SQLException;
    }

    private record BatchItem(int index, BaseInfo info) {}

    static void execute_batch(
        Connection              connect,
        Set<? extends BaseInfo> info_set,
        String                  sql,
        ParamSetter             setter,
        int                     chunk_size
    ) throws SQLException {

        if(chunk_size <= 0) throw new IllegalArgumentException("chunk_size must be greater than 0");

        try(var ps = connect.prepareStatement(sql)) {
            var count = 0;
            var batch_items = new ArrayList<BatchItem>();
            for(var info : info_set) {
                var batch_item = new BatchItem(++count, info);
                try {
                    setter.set(ps, info);
                    ps.addBatch();
                    batch_items.add(batch_item);
                } catch(SQLException e) {
                    print_info_failure("设置数据库参数", batch_item, e);
                    throw e;
                }

                // 当批次达到指定大小时执行批量操作，并清理批次数据
                if(batch_items.size() == chunk_size) {
                    execute_batch_chunk(connect, ps, sql, setter, batch_items);
                    batch_items.clear();
                }
            }

            // 执行剩余的批次数据
            if(!batch_items.isEmpty()) execute_batch_chunk(connect, ps, sql, setter, batch_items);
        }
    }

    private static void execute_batch_chunk(
        Connection      connect,
        PreparedStatement ps,
        String          sql,
        ParamSetter     setter,
        List<BatchItem> batch_items
    ) throws SQLException {

        SQLException batch_error = null;
        try { ps.executeBatch(); }
        catch(SQLException e) {
            batch_error = e;
            System.err.println("数据库批量写入失败，正在定位问题数据项...");

            var found_failure = false;
            try(var diagnostic_ps = connect.prepareStatement(sql)) {
                for(var item : batch_items) {
                    try {
                        setter.set(diagnostic_ps, item.info());
                        diagnostic_ps.executeUpdate();
                    } catch(SQLException item_error) {
                        found_failure = true;
                        print_info_failure("执行数据库写入", item, item_error);
                    } finally {
                        try { diagnostic_ps.clearParameters(); }
                        catch(SQLException _) {}
                    }
                }
            } catch(SQLException diagnostic_error) {
                System.err.println("数据库写入失败: 无法逐条定位问题数据项: " + diagnostic_error.getMessage());
            }

            if(!found_failure) {
                System.err.println("数据库写入失败: 未能定位单个失败项，以下批次数据可能相关");
                for(var item : batch_items) print_info_item("候选数据项", item);
            }
            throw e;
        }
        finally {
            try { ps.clearBatch(); }
            catch(SQLException clear_error) {
                if(batch_error != null) batch_error.addSuppressed(clear_error);
                else throw clear_error;
            }
        }
    }

    private static void print_info_failure(String action, BatchItem item, Exception error) {
        System.err.println("数据库写入失败: " + action);
        System.err.println("错误信息: " + error.getMessage());
        print_info_item("问题数据项", item);
    }

    private static void print_info_item(String title, BatchItem item) {
        var info = item.info();
        var type_name = info == null ? "null" : info.getClass().getSimpleName();
        System.err.println(title + " #" + item.index() + ": " + type_name);
        if(info == null) {
            System.err.println("null");
            return;
        }
        try { System.err.println(info.toPrintString("", false)); }
        catch(RuntimeException _) { System.err.println(info); }
    }
}
