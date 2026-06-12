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
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;


public class SQLiteAccess implements Closeable {

    private static final int SQL_PARAM_CHUNK_SIZE = 500;

    private final Connection conn;

    public SQLiteAccess(String dbPath) throws SQLException {

        var dbUrl = "jdbc:sqlite:" + dbPath;
        if(!new File(dbPath).exists()) {
            System.out.println("该数据库文件不存在: " + dbPath);
            SQLiteInit.initDatabase(dbUrl);
        }

        conn = DriverManager.getConnection(dbUrl);
        applyPragmaSettings();
    }

    private void applyPragmaSettings() {
        try(var st = conn.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON;");
            st.execute("PRAGMA journal_mode = WAL;");
            st.execute("PRAGMA synchronous = NORMAL;");
            st.execute("PRAGMA temp_store = MEMORY;");
            st.execute("PRAGMA cache_size = 10000;");
        } catch(SQLException e) {
            System.err.println("Failed to apply PRAGMA settings: " + e.getMessage());
        }
    }

    public void UpsertAnimeInfo(AnimeInfo item) throws SQLException {
        if(item == null) return;
        try(var ps = AnimeInfo.GetUpsertStatement(conn)) {
            item.SetParams(ps);
            ps.executeUpdate();
        }
    }

    public void UpsertAnimeInfo(Collection<AnimeInfo> items) throws SQLException {
        runBatch(items, AnimeInfo.GetUpsertStatement(conn), AnimeInfo::SetParams);
    }

    public void UpsertEpisodeInfo(EpisodeInfo item) throws SQLException {
        if(item == null) return;
        try(var ps = EpisodeInfo.GetUpsertStatement(conn)) {
            item.SetParams(ps);
            ps.executeUpdate();
        }
    }

    public void UpsertEpisodeInfo(Collection<EpisodeInfo> items) throws SQLException {
        runBatch(items, EpisodeInfo.GetUpsertStatement(conn), EpisodeInfo::SetParams);
    }

    public void UpsertEpisodeRecordInfo(EpisodeRecordInfo item) throws SQLException {
        if(item == null) return;
        try(var ps = EpisodeRecordInfo.GetUpsertStatement(conn)) {
            item.SetParams(ps);
            ps.executeUpdate();
        }
    }

    public void UpsertEpisodeRecordInfo(Collection<EpisodeRecordInfo> items) throws SQLException {
        runBatch(items, EpisodeRecordInfo.GetUpsertStatement(conn), EpisodeRecordInfo::SetParams);
    }

    public void UpsertRSSInfo(RSSInfo item) throws SQLException {
        if(item == null) return;
        try(var ps = RSSInfo.GetUpsertStatement(conn)) {
            item.SetParams(ps);
            ps.executeUpdate();
        }
    }

    public void UpsertRSSInfo(Collection<RSSInfo> items) throws SQLException {
        runBatch(items, RSSInfo.GetUpsertStatement(conn), RSSInfo::SetParams);
    }

    public void UpsertTorrentPageInfo(TorrentPageInfo item) throws SQLException {
        if(item == null) return;
        try(var ps = TorrentPageInfo.GetUpsertStatement(conn)) {
            item.SetParams(ps);
            ps.executeUpdate();
        }
    }

    public void UpsertTorrentPageInfo(Collection<TorrentPageInfo> items) throws SQLException {
        runBatch(items, TorrentPageInfo.GetUpsertStatement(conn), TorrentPageInfo::SetParams);
    }

    public void UpsertTorrentInfo(TorrentInfo item) throws SQLException {
        if(item == null) return;
        try(var ps = TorrentInfo.GetUpsertStatement(conn)) {
            item.SetParams(ps);
            ps.executeUpdate();
        }
    }

    public void UpsertTorrentInfo(Collection<TorrentInfo> items) throws SQLException {
        runBatch(items, TorrentInfo.GetUpsertStatement(conn), TorrentInfo::SetParams);
    }

    private <T> void runBatch(Collection<T> items, PreparedStatement ps, SQLBinder<T> binder) throws SQLException {
        if(items == null || items.isEmpty()) {
            ps.close();
            return;
        }

        var prevAuto = conn.getAutoCommit();
        conn.setAutoCommit(false);
        try(ps) {
            var count = 0;
            for(var item : items) {
                if(item == null) continue;
                binder.bind(item, ps);
                ps.addBatch();
                count++;
                if(count % SQL_PARAM_CHUNK_SIZE == 0) ps.executeBatch();
            }
            if(count > 0) ps.executeBatch();
            conn.commit();
        } catch(SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(prevAuto);
        }
    }

    public void ExportTorrentFiles(Set<String> torHashList, String safePath) {
        if(torHashList == null || torHashList.isEmpty()) return;

        System.out.println("正在导出种子文件: " + torHashList.size() + " 个，保存路径: " + safePath);

        for(var chunk : chunks(normalizeHashes(torHashList))) {
            var placeholders = String.join(",", java.util.Collections.nCopies(chunk.size(), "?"));
            var sql = "SELECT TOR_HASH, torrent_file FROM torrent WHERE TOR_HASH IN (" + placeholders + ")";

            try(var ps = conn.prepareStatement(sql)) {
                bindStrings(ps, chunk);
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
        var uniqueHashes = normalizeHashes(hashList);
        if(uniqueHashes.isEmpty()) return Set.of();

        var hasFileSet = new HashSet<String>();
        for(var chunk : chunks(uniqueHashes)) {
            var placeholders = String.join(",", java.util.Collections.nCopies(chunk.size(), "?"));
            var sql
            = "SELECT TOR_HASH FROM torrent "
            + "WHERE TOR_HASH IN (" + placeholders + ") "
            + "AND torrent_file IS NOT NULL "
            + "AND length(torrent_file) > 0";

            try(var ps = conn.prepareStatement(sql)) {
                bindStrings(ps, chunk);
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
        var uniqueHashes = normalizeHashes(hashList);
        if(uniqueHashes.isEmpty()) return Set.of();

        var result = new LinkedHashMap<String, List<String>>();
        for(var hash : uniqueHashes) result.put(hash, new ArrayList<>());

        for(var chunk : chunks(uniqueHashes)) {
            var placeholders = String.join(",", java.util.Collections.nCopies(chunk.size(), "?"));
            var sql = "SELECT TOR_HASH, url_download FROM torrent_page WHERE TOR_HASH IN (" + placeholders + ")";

            try(var ps = conn.prepareStatement(sql)) {
                bindStrings(ps, chunk);
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

    private static LinkedHashSet<String> normalizeHashes(Collection<String> hashes) {
        var uniqueHashes = new LinkedHashSet<String>();
        if(hashes == null) return uniqueHashes;

        for(var hash : hashes) {
            if(hash != null && !hash.isBlank()) uniqueHashes.add(hash);
        }
        return uniqueHashes;
    }

    private static List<List<String>> chunks(Collection<String> values) {
        var input = new ArrayList<>(values);
        var res   = new ArrayList<List<String>>();
        for(var start = 0; start < input.size(); start += SQL_PARAM_CHUNK_SIZE) {
            var end = Math.min(start + SQL_PARAM_CHUNK_SIZE, input.size());
            res.add(input.subList(start, end));
        }
        return res;
    }

    private static void bindStrings(PreparedStatement ps, List<String> values) throws SQLException {
        for(var i = 0; i < values.size(); i++) {
            ps.setString(i + 1, values.get(i));
        }
    }

    @Override
    public void close() {
        if(conn != null) {
            try {
                conn.close();
            } catch(SQLException e) {
                System.err.println("Close failed: " + e.getMessage());
            }
        }
    }

    @FunctionalInterface
    private interface SQLBinder<T> {
        void bind(T item, PreparedStatement ps) throws SQLException;
    }
}
