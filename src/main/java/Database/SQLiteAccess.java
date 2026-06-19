package Database;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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

        connect = DriverManager.getConnection(db_url); // 建立数据库连接

        try {

            // 应用PRAGMA设置以优化性能和安全性
            try(var st = connect.createStatement()) {
                for(var sql : SQLiteSQL.PRAGMA_SETTINGS) st.execute(sql);
            }

            // 如果数据库文件不存在，则创建表结构
            if(!db_exists) DatabaseUtils.initialize_database_schema(connect);

            // 验证以确保结构正确
            DatabaseUtils.validate_database_schema(connect);

        }

        // 捕获SQLException和RuntimeException，确保在发生任何异常时都能正确关闭数据库连接
        catch(SQLException | RuntimeException e) {
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

            if     (info instanceof AnimeInfo         i) animeInfoSet        .add(i);
            else if(info instanceof EpisodeInfo       i) episodeInfoSet      .add(i);
            else if(info instanceof EpisodeRecordInfo i) episodeRecordInfoSet.add(i);
            else if(info instanceof RSSInfo           i) rssInfoSet          .add(i);
            else if(info instanceof TorrentPageInfo   i) torrentPageInfoSet  .add(i);
            else if(info instanceof TorrentInfo       i) torrentInfoSet      .add(i);

            else throw new IllegalArgumentException("Unsupported Info type: " + info.getClass().getName());
        }

        // 事务处理
        var prev_auto = connect.getAutoCommit();
        connect.setAutoCommit(false);
        try {
            var failures = new ArrayList<Transactions.UpsertFailure>();

            System.out.println("正在更新数据库，AnimeInfo: " + animeInfoSet.size());
            Transactions.upsertInfoAnimeInfo        (connect, animeInfoSet, failures);
            System.out.println("正在更新数据库，EpisodeInfo: " + episodeInfoSet.size());
            Transactions.upsertInfoEpisodeInfo      (connect, episodeInfoSet, failures);
            System.out.println("正在更新数据库，EpisodeRecordInfo: " + episodeRecordInfoSet.size());
            Transactions.upsertInfoEpisodeRecordInfo(connect, episodeRecordInfoSet, failures);
            System.out.println("正在更新数据库，RSSInfo: " + rssInfoSet.size());
            Transactions.upsertInfoRSSInfo          (connect, rssInfoSet, failures);
            System.out.println("正在更新数据库，TorrentPageInfo: " + torrentPageInfoSet.size());
            Transactions.upsertInfoTorrentPageInfo  (connect, torrentPageInfoSet, failures);
            System.out.println("正在更新数据库，TorrentInfo: " + torrentInfoSet.size());
            Transactions.upsertInfoTorrentInfo      (connect, torrentInfoSet, failures);

            if(failures.isEmpty()) {
                connect.commit();
                System.out.println("数据库更新完成");
            } else {
                System.err.println("数据库写入完成，共有 " + failures.size() + " 个数据项写入失败：");
                for(var i = 0; i < failures.size(); i++) {
                    var failure = failures.get(i);
                    var info = failure.info();
                    var type_name = info == null ? "null" : info.getClass().getSimpleName();
                    System.err.println("问题数据项 #" + (i + 1) + ": " + type_name);
                    System.err.println("错误信息: " + failure.error().getMessage());
                    if(info == null) System.err.println("null");
                    else try { System.err.println(info.toPrintString("", false)); }
                    catch(RuntimeException _) { System.err.println(info); }
                }

                var reader = new BufferedReader(new InputStreamReader(System.in));
                var commit_successful_items = false;
                while(true) {
                    System.out.print("是否提交其他插入成功的数据？[y/n]: ");
                    System.out.flush();

                    final String answer;
                    try { answer = reader.readLine(); }
                    catch(IOException e) { throw new SQLException("读取提交确认失败", e); }

                    if(answer == null) {
                        System.out.println("未读取到用户输入，默认不提交。");
                        break;
                    }

                    var normalized_answer = answer.trim().toLowerCase();
                    if(normalized_answer.equals("y") || normalized_answer.equals("yes")) {
                        commit_successful_items = true;
                        break;
                    }
                    if(normalized_answer.equals("n") || normalized_answer.equals("no")) break;
                    System.out.println("请输入 y 或 n。");
                }

                if(commit_successful_items) {
                    connect.commit();
                    System.out.println("已提交其他插入成功的数据。");
                } else {
                    connect.rollback();
                    System.out.println("已取消提交，本次数据库更新已全部回滚。");
                }
            }
        }
        catch(SQLException | RuntimeException e) { connect.rollback(); throw e; }
        finally { connect.setAutoCommit(prev_auto); }
    }

    // 在同一个事务中整表替换视图使用的ANI_ID和RSS URL筛选条件
    public void ReplaceRequiredViewFilters(
        Set<Integer> ani_id_set,
        Set<String>  rss_url_set
    ) throws SQLException {

        if(ani_id_set == null) throw new IllegalArgumentException("ANI_ID set cannot be null");
        if(rss_url_set == null) throw new IllegalArgumentException("RSS URL set cannot be null");
        Transactions.replaceViewFilters(connect, ani_id_set, rss_url_set);
    }

    public void FlushDatabaseViews() throws SQLException {
        DatabaseUtils.recreate_database_views(connect);
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

    @Override
    public void close() {
        if(connect != null) try { connect.close(); }
        catch(SQLException e) { System.err.println("Close failed: " + e.getMessage()); }
    }

}
