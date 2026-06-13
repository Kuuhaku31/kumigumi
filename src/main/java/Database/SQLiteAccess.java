// cSpell:words jdbc

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
            run_info_batch(animeInfoSet,         AnimeInfo.class        );
            run_info_batch(episodeInfoSet,       EpisodeInfo.class      );
            run_info_batch(episodeRecordInfoSet, EpisodeRecordInfo.class);
            run_info_batch(rssInfoSet,           RSSInfo.class          );
            run_info_batch(torrentPageInfoSet,   TorrentPageInfo.class  );
            run_info_batch(torrentInfoSet,       TorrentInfo.class      );
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

    private <T extends BaseInfo> void run_info_batch(Set<T> items, Class<T> info_type) throws SQLException {

        // 参数检查
        if(items.isEmpty()) return;

        // 获取对应的PreparedStatement，并为每个Info对象设置参数并添加到批处理中
        try(var ps = connect.prepareStatement(SQLiteSQL.upsertInfo(info_type))) {

            var count = 0;
            for(var item : items) {
                if(item == null) continue;
                item.setParams(ps);
                ps.addBatch();
                count++;
                if(count % SQL_PARAM_CHUNK_SIZE == 0) ps.executeBatch();
            }

            // 执行剩余的批处理
            if(count > 0) ps.executeBatch();
        }
    }


    @Override
    public void close() {
        if(connect != null) try { connect.close(); }
        catch(SQLException e) { System.err.println("Close failed: " + e.getMessage()); }
    }

}
