// cSpell:words jdbc

package Database;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import Database.Info.BaseInfo;
import Database.Info.RSSInfo;
import Database.Info.TorrentInfo;
import Database.Info.TorrentPageInfo;
import Database.Info.AnimeInfo;
import Database.Info.EpisodeInfo;
import Database.Info.EpisodeRecordInfo;
import Utils.DatabaseUtils;


public class SQLiteAccess implements Closeable {

    private final int SQL_PARAM_CHUNK_SIZE = 500;

    private final Connection connect;

    public SQLiteAccess(String dbPath) throws SQLException {

        // 建立数据库连接，如果数据库文件不存在则创建新数据库并初始化表结构
        var db_url = "jdbc:sqlite:" + dbPath;
        if(!new File(dbPath).exists()) {
            System.out.println("该数据库文件不存在: " + dbPath);
            System.out.println("创建数据库...");
            try(var conn = DriverManager.getConnection(db_url)) {
                for(var sql : SQLiteSQL.createTableStatements()) {
                    conn.prepareStatement(sql).execute();
                }
            }
            System.out.println("数据库创建完成");
        }
        connect = DriverManager.getConnection(db_url);

        // 应用PRAGMA设置以优化性能和安全性
        try(var st = connect.createStatement()) {
            for(var sql : SQLiteSQL.PRAGMA_SETTINGS) st.execute(sql);
        } catch(SQLException e) {
            System.err.println("Failed to apply PRAGMA settings: " + e.getMessage());
        }
    }

    public void UpsertInfo(Set<? extends BaseInfo> info_set) throws SQLException {

        // 参数检查
        if(info_set == null || info_set.isEmpty()) return;

        // 一次性分类，避免按类型重复扫描原始集合
        var animeInfoSet         = new LinkedHashSet<AnimeInfo>();
        var episodeInfoSet       = new LinkedHashSet<EpisodeInfo>();
        var episodeRecordInfoSet = new LinkedHashSet<EpisodeRecordInfo>();
        var rssInfoSet           = new LinkedHashSet<RSSInfo>();
        var torrentPageInfoSet   = new LinkedHashSet<TorrentPageInfo>();
        var torrentInfoSet       = new LinkedHashSet<TorrentInfo>();
        for(var info : info_set) {

            if(info == null) continue;

            if     (info instanceof AnimeInfo         i) animeInfoSet        .add(i);
            else if(info instanceof EpisodeInfo       i) episodeInfoSet      .add(i);
            else if(info instanceof EpisodeRecordInfo i) episodeRecordInfoSet.add(i);
            else if(info instanceof RSSInfo           i) rssInfoSet          .add(i);
            else if(info instanceof TorrentPageInfo   i) torrentPageInfoSet  .add(i);
            else if(info instanceof TorrentInfo       i) torrentInfoSet      .add(i);

            else throw new IllegalArgumentException("Unsupported Info type: " + info.getClass().getName());
        }

        // 事务处理，确保批量插入的原子性和性能
        var prev_auto = connect.getAutoCommit();
        connect.setAutoCommit(false);
        try {
            run_info_batch(animeInfoSet,         SQLiteSQL.UPSERT_ANIME_INFO,          SQLiteAccess::set_params_anime_info         );
            run_info_batch(episodeInfoSet,       SQLiteSQL.UPSERT_EPISODE_INFO,        SQLiteAccess::set_params_episode_info       );
            run_info_batch(episodeRecordInfoSet, SQLiteSQL.UPSERT_EPISODE_RECORD_INFO, SQLiteAccess::set_params_episode_record_info);
            run_info_batch(rssInfoSet,           SQLiteSQL.UPSERT_RSS_INFO,            SQLiteAccess::set_params_rss_info           );
            run_info_batch(torrentPageInfoSet,   SQLiteSQL.UPSERT_TORRENT_PAGE_INFO,   SQLiteAccess::set_params_torrent_page_info  );
            run_info_batch(torrentInfoSet,       SQLiteSQL.UPSERT_TORRENT_INFO,        SQLiteAccess::set_params_torrent_info       );
            connect.commit();
        }
        catch(SQLException | RuntimeException e) { connect.rollback(); throw e; }
        finally { connect.setAutoCommit(prev_auto); }
    }

    public void ExportTorrentFiles(Set<String> torHashList, String safePath) {
        if(torHashList == null || torHashList.isEmpty()) return;

        System.out.println("正在导出种子文件: " + torHashList.size() + " 个，保存路径: " + safePath);

        for(var chunk : DatabaseUtils.chunks(DatabaseUtils.normalizeHashes(torHashList), SQL_PARAM_CHUNK_SIZE)) {
            var sql = SQLiteSQL.selectTorrentFilesByHashCount(chunk.size());

            try(var ps = connect.prepareStatement(sql)) {
                DatabaseUtils.bindStrings(ps, chunk);
                try(var rs = ps.executeQuery()) {
                    while(rs.next()) {
                        var torHash = rs.getString("TOR_HASH");
                        var fileBytes = rs.getBytes("torrent_file");
                        if(fileBytes == null || fileBytes.length == 0) continue;

                        var filePath = safePath + File.separator + torHash + ".torrent";
                        try(var fos = new FileOutputStream(filePath)) {
                            fos.write(fileBytes);
                        } catch(IOException e) {
                            System.err.println("Failed to save torrent file for hash: " + torHash + ", error: " + e.getMessage());
                        }
                    }
                }
            } catch(SQLException e) {
                System.err.println("Failed to query torrent files: " + e.getMessage());
            }
        }

        System.out.println("种子文件导出完成");
    }

    public Set<String> GetTorrentHashNotExist(Set<String> hashList) throws SQLException {
        var uniqueHashes = DatabaseUtils.normalizeHashes(hashList);
        if(uniqueHashes.isEmpty()) return Set.of();

        var hasFileSet = new HashSet<String>();
        for(var chunk : DatabaseUtils.chunks(uniqueHashes, SQL_PARAM_CHUNK_SIZE)) {
            var sql = SQLiteSQL.selectExistingTorrentHashesByHashCount(chunk.size());

            try(var ps = connect.prepareStatement(sql)) {
                DatabaseUtils.bindStrings(ps, chunk);
                try(var rs = ps.executeQuery()) {
                    while(rs.next()) hasFileSet.add(rs.getString("TOR_HASH"));
                }
            }
        }

        Set<String> notExistSet = new LinkedHashSet<>();
        for(var hash : uniqueHashes) {
            if(!hasFileSet.contains(hash)) notExistSet.add(hash);
        }
        return notExistSet;
    }

    public Set<TorrentDownloader> GetDownloaderByHash(Set<String> hashList) throws SQLException {
        var uniqueHashes = DatabaseUtils.normalizeHashes(hashList);
        if(uniqueHashes.isEmpty()) return Set.of();

        var result = new LinkedHashMap<String, List<String>>();
        for(var hash : uniqueHashes) result.put(hash, new ArrayList<>());

        for(var chunk : DatabaseUtils.chunks(uniqueHashes, SQL_PARAM_CHUNK_SIZE)) {
            var sql = SQLiteSQL.selectDownloadUrlsByHashCount(chunk.size());

            try(var ps = connect.prepareStatement(sql)) {
                DatabaseUtils.bindStrings(ps, chunk);
                try(var rs = ps.executeQuery()) {
                    while(rs.next()) {
                        var hash = rs.getString("TOR_HASH");
                        var url  = rs.getString("url_download");
                        if(url != null && result.containsKey(hash)) result.get(hash).add(url);
                    }
                }
            }
        }

        Set<TorrentDownloader> downloaderSet = new LinkedHashSet<>();
        for(var entry : result.entrySet()) {
            downloaderSet.add(new TorrentDownloader(entry.getKey(), entry.getValue()));
        }
        return downloaderSet;
    }


    @FunctionalInterface
    private interface InfoParamSetter<T extends BaseInfo> {
        void set_params(PreparedStatement ps, T info) throws SQLException;
    }

    private <T extends BaseInfo> void run_info_batch(Set<T> items, String sql_str, InfoParamSetter<T> set_params) throws SQLException {

        // 参数检查
        if(items.isEmpty()) return;

        // 获取对应的PreparedStatement，并为每个Info对象设置参数并添加到批处理中
        try(var ps = connect.prepareStatement(sql_str)) {

            var count = 0;
            for(var item : items) {
                if(item == null) continue;
                set_params.set_params(ps, item);
                ps.addBatch();
                count++;
                if(count % SQL_PARAM_CHUNK_SIZE == 0) ps.executeBatch();
            }

            // 执行剩余的批处理
            if(count > 0) ps.executeBatch();
        }
    }

    private static void set_params_anime_info(PreparedStatement ps, AnimeInfo info) throws SQLException {
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
    }

    private static void set_params_episode_info(PreparedStatement ps, EpisodeInfo info) throws SQLException {
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
    }

    private static void set_params_episode_record_info(PreparedStatement ps, EpisodeRecordInfo info) throws SQLException {
        DatabaseUtils.safeSetInt            (ps, 1, info.EPI_ID       );
        DatabaseUtils.safeSetOffsetDateTime (ps, 2, info.view_datetime);
        DatabaseUtils.safeSetInt            (ps, 3, info.rating       );
        DatabaseUtils.safeSetString         (ps, 4, info.comment      );
    }

    private static void set_params_rss_info(PreparedStatement ps, RSSInfo info) throws SQLException {
        DatabaseUtils.safeSetString (ps, 1, info.URL_RSS);
        DatabaseUtils.safeSetInt    (ps, 2, info.ANI_ID );
    }

    private static void set_params_torrent_page_info(PreparedStatement ps, TorrentPageInfo info) throws SQLException {
        DatabaseUtils.safeSetString         (ps, 1, info.URL_RSS        );
        DatabaseUtils.safeSetString         (ps, 2, info.TOR_HASH       );
        DatabaseUtils.safeSetOffsetDateTime (ps, 3, info.air_datetime   );
        DatabaseUtils.safeSetString         (ps, 4, info.url_download   );
        DatabaseUtils.safeSetString         (ps, 5, info.url_page       );
        DatabaseUtils.safeSetString         (ps, 6, info.title          );
        DatabaseUtils.safeSetString         (ps, 7, info.subtitle_group );
        DatabaseUtils.safeSetString         (ps, 8, info.description    );
        DatabaseUtils.safeSetOffsetDateTime (ps, 9, info.update_datetime);
    }

    private static void set_params_torrent_info(PreparedStatement ps, TorrentInfo info) throws SQLException {
        DatabaseUtils.safeSetString (ps, 1, info.TOR_HASH    );
        DatabaseUtils.safeSetString (ps, 2, info.file_name   );
        DatabaseUtils.safeSetLong   (ps, 3, info.file_size   );
        DatabaseUtils.safeSetBytes  (ps, 4, info.torrent_file);
    }


    @Override
    public void close() {
        if(connect != null) try { connect.close(); }
        catch(SQLException e) { System.err.println("Close failed: " + e.getMessage()); }
    }

}
