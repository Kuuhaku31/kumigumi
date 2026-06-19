package Database;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
            System.out.println("正在更新数据库，AnimeInfo: " + animeInfoSet.size());
            Transactions.upsertInfoAnimeInfo        (connect, animeInfoSet);
            System.out.println("正在更新数据库，EpisodeInfo: " + episodeInfoSet.size());
            Transactions.upsertInfoEpisodeInfo      (connect, episodeInfoSet);
            System.out.println("正在更新数据库，EpisodeRecordInfo: " + episodeRecordInfoSet.size());
            Transactions.upsertInfoEpisodeRecordInfo(connect, episodeRecordInfoSet);
            System.out.println("正在更新数据库，RSSInfo: " + rssInfoSet.size());
            Transactions.upsertInfoRSSInfo          (connect, rssInfoSet);
            System.out.println("正在更新数据库，TorrentPageInfo: " + torrentPageInfoSet.size());
            Transactions.upsertInfoTorrentPageInfo  (connect, torrentPageInfoSet);
            System.out.println("正在更新数据库，TorrentInfo: " + torrentInfoSet.size());
            Transactions.upsertInfoTorrentInfo      (connect, torrentInfoSet);
            connect.commit();
            System.out.println("数据库更新完成");
        }
        catch(SQLException | RuntimeException e) { connect.rollback(); throw e; }
        finally { connect.setAutoCommit(prev_auto); }
    }

    // 替换required_anime_id表中的ANI_ID列表，先删除原有数据再逐条插入新数据
    public void ReplaceRequiredAnimeIds(Set<Integer> ani_id_set) throws SQLException {

        Transactions.replaceAniIds(connect, ani_id_set);
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
