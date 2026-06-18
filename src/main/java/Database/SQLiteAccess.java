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
import java.util.TreeSet;

import Info.AnimeInfo;
import Info.BaseInfo;
import Info.EpisodeInfo;
import Info.EpisodeRecordInfo;
import Info.RSSInfo;
import Info.TorrentInfo;
import Info.TorrentPageInfo;


public class SQLiteAccess implements Closeable {

    private final int SQL_PARAM_CHUNK_SIZE = 500;

    private final Connection connect;

    /**
     * 构造函数，建立数据库连接并确保数据库文件和表结构存在
     * @param db_path
     * @throws SQLException
     * <hr>
     * 先检查数据库文件是否存在
     * <p>
     * - 不存在：如果不存在则创建新数据库并执行表结构初始化SQL语句
     * <p>
     * - 存在：检查各个表和视图是否存在且结构正确
     * <p>
     * - 如果表或视图缺失或结构不正确，直接报错并退出程序
     */
    public SQLiteAccess(String db_path) throws SQLException {

        // 参数检查
        var db_file = new File(db_path);
        var db_exists = db_file.exists();
        if(db_exists && !db_file.isFile()) {
            throw new SQLException("数据库路径不是文件: " + db_path);
        }

        var db_url = "jdbc:sqlite:" + db_path;
        if(!db_exists) {
            System.out.println("该数据库文件不存在: " + db_path);
            System.out.println("创建数据库...");
        }

        connect = DriverManager.getConnection(db_url);
        try {

            // 应用PRAGMA设置以优化性能和安全性
            try(var st = connect.createStatement()) {
                for(var sql : SQLiteSQL.PRAGMA_SETTINGS) st.execute(sql);
            }

            // 如果数据库文件已存在，则验证表结构；如果不存在，则创建表结构后再验证
            if(!db_exists) DatabaseUtils.initialize_database_schema(connect); // 创建数据库表和视图
            DatabaseUtils.validate_database_schema(connect);   // 再次验证以确保结构正确

        } catch(SQLException | RuntimeException e) {
            try { connect.close(); }
            catch(SQLException close_error) { e.addSuppressed(close_error); }
            throw e;
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

    // 替换required_anime_id表中的ANI_ID列表，先删除原有数据再批量插入新数据
    public void ReplaceRequiredAnimeIds(Set<Integer> aniIdSet) throws SQLException {

        // 参数检查
        if(aniIdSet == null) throw new IllegalArgumentException("ANI_ID set cannot be null");
        for(var aniId : aniIdSet) {
            if(aniId == null) throw new IllegalArgumentException("ANI_ID set cannot contain null");
        }

        var prev_auto = connect.getAutoCommit();
        connect.setAutoCommit(false);
        try(var deleteStatement = connect.createStatement();
            var insertStatement = connect.prepareStatement(SQLiteSQL.INSERT_REQUIRED_ANIME_ID)) {
            deleteStatement.executeUpdate(SQLiteSQL.DELETE_REQUIRED_ANIME_IDS);
            for(var aniId : new TreeSet<>(aniIdSet)) {
                insertStatement.setInt(1, aniId);
                insertStatement.addBatch();
            }
            insertStatement.executeBatch();
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

    // 用于批量处理的内部类，封装了Info对象及其在批处理中的索引
    private record InfoBatchItem<T extends BaseInfo>(int index, T info) {}

    private <T extends BaseInfo> void run_info_batch(Set<T> items, String sql_str, InfoParamSetter<T> set_params) throws SQLException {

        // 参数检查
        if(items.isEmpty()) return;

        // 获取对应的PreparedStatement，并为每个Info对象设置参数并添加到批处理中
        try(var ps = connect.prepareStatement(sql_str)) {

            var item_index = 0;
            var batch_items = new ArrayList<InfoBatchItem<T>>();
            for(var item : items) {
                if(item == null) continue;
                item_index++;

                try {
                    set_params.set_params(ps, item);
                    ps.addBatch();
                    batch_items.add(new InfoBatchItem<>(item_index, item));
                } catch(SQLException | RuntimeException e) {
                    print_info_item_failure("设置数据库参数", item_index, item, e);
                    throw e;
                }

                if(batch_items.size() == SQL_PARAM_CHUNK_SIZE) {
                    execute_info_batch(ps, sql_str, set_params, batch_items);
                    batch_items.clear();
                }
            }

            // 执行剩余的批处理
            if(!batch_items.isEmpty()) execute_info_batch(ps, sql_str, set_params, batch_items);
        }
    }

    // 执行批量操作，并在失败时尝试逐条定位问题数据项
    private <T extends BaseInfo> void execute_info_batch(
        PreparedStatement      ps,          // PreparedStatement对象
        String                 sql_str,     // SQL语句字符串
        InfoParamSetter<T>     set_params,  // 参数设置器，用于为每个Info对象设置PreparedStatement参数
        List<InfoBatchItem<T>> batch_items  // 批处理的Info对象列表，每个对象包含其在批处理中的索引和对应的Info对象
    ) throws SQLException {

        SQLException batch_error = null; // 用于捕获批量执行时的异常
        try { ps.executeBatch(); }
        catch(SQLException e) {
            batch_error = e;
            System.err.println("数据库批量写入失败，正在定位问题数据项...");

            // 如果逐条定位失败，则打印整个批次的候选数据项
            if(!print_individual_info_failures(sql_str, set_params, batch_items)) {
                print_info_batch_candidates(batch_items, e);
            }

            throw e;
        } finally {
            try { ps.clearBatch(); }
            catch(SQLException e) {
                if(batch_error != null) batch_error.addSuppressed(e);
                else throw e;
            }
        }
    }

    // 执行逐条写入操作，并在失败时打印问题数据项的详细信息
    private <T extends BaseInfo> boolean print_individual_info_failures(
        String sql_str,
        InfoParamSetter<T> set_params,
        List<InfoBatchItem<T>> batch_items
    ) {

        var found_failure = false;
        try(var ps = connect.prepareStatement(sql_str)) {
            for(var batch_item : batch_items) {
                try {
                    set_params.set_params(ps, batch_item.info());
                    ps.executeUpdate();
                } catch(SQLException | RuntimeException e) {
                    found_failure = true;
                    print_info_item_failure("执行数据库写入", batch_item.index(), batch_item.info(), e);
                } finally {
                    try { ps.clearParameters(); }
                    catch(SQLException _) {}
                }
            }
        } catch(SQLException e) {
            System.err.println("数据库写入失败: 无法逐条定位问题数据项: " + e.getMessage());
        }
        return found_failure;
    }

    private static <T extends BaseInfo> void print_info_batch_candidates(List<InfoBatchItem<T>> batch_items, SQLException e) {
        System.err.println("数据库写入失败: 未能定位单个失败项，以下批次数据可能相关");
        System.err.println("错误信息: " + e.getMessage());
        for(var batch_item : batch_items) {
            print_info_item("候选数据项", batch_item.index(), batch_item.info());
        }
    }

    private static void print_info_item_failure(String action, int item_index, BaseInfo item, Exception e) {
        System.err.println("数据库写入失败: " + action);
        System.err.println("错误信息: " + e.getMessage());
        print_info_item("问题数据项", item_index, item);
    }

    private static void print_info_item(String title, int item_index, BaseInfo item) {
        System.err.println(title + " #" + item_index + ": " + info_type_name(item));
        System.err.println(info_to_log_string(item));
    }

    private static String info_type_name(BaseInfo item) {
        return item == null ? "null" : item.getClass().getSimpleName();
    }

    private static String info_to_log_string(BaseInfo item) {
        if(item == null) return "null";
        try { return item.toPrintString("", false); }
        catch(RuntimeException _) { return String.valueOf(item); }
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
