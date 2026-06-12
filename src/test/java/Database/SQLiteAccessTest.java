package Database;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.DriverManager;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

class SQLiteAccessTest {

    @TempDir
    Path tempDir;

    @Test
    void initializesDatabaseAndUpsertsCurrentInfoTypes() throws Exception {
        var dbPath = tempDir.resolve("kumigumi-test.db").toString();
        var torrent = new TorrentInfo(DatabaseInfoTest.sampleTorrent());

        try(var db = new SQLiteAccess(dbPath)) {
            db.UpsertAnimeInfo(new AnimeInfo(Map.of(
                "ANI_ID", "100",
                "title", "Anime",
                "episode_count", "12"
            )));
            db.UpsertEpisodeInfo(List.of(new EpisodeInfo(Map.of(
                "EPI_ID", "200",
                "ANI_ID", "100",
                "ep", "1",
                "sort", "1",
                "duration", "1440",
                "title", "Episode"
            ))));
            db.UpsertEpisodeRecordInfo(new EpisodeRecordInfo(
                200,
                OffsetDateTime.parse("2026-01-04T21:30:00+09:00"),
                5,
                "watched"
            ));
            db.UpsertRSSInfo(new RSSInfo("https://example.com/feed.xml", 100));
            db.UpsertTorrentPageInfo(new TorrentPageInfo(
                "https://example.com/feed.xml",
                torrent.TOR_HASH,
                OffsetDateTime.parse("2026-01-05T12:00:00+09:00"),
                "https://example.com/download.torrent",
                "https://example.com/page",
                "Title",
                "Group",
                "Description"
            ));
            db.UpsertTorrentInfo(torrent);

            assertEquals(Set.of("missing"), db.GetTorrentHashNotExist(Set.of(torrent.TOR_HASH, "missing")));

            var downloaders = db.GetDownloaderByHash(Set.of(torrent.TOR_HASH));
            assertEquals(1, downloaders.size());
            var downloader = downloaders.iterator().next();
            assertEquals(torrent.TOR_HASH, downloader.TOR_HASH());
            assertEquals(List.of("https://example.com/download.torrent"), downloader.url_download_list());
        }

        try(var conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            var stmt = conn.createStatement()) {
            assertEquals(1, count(stmt, "anime"));
            assertEquals(1, count(stmt, "episode"));
            assertEquals(1, count(stmt, "episode_record"));
            assertEquals(1, count(stmt, "rss"));
            assertEquals(1, count(stmt, "torrent_page"));
            assertEquals(1, count(stmt, "torrent"));
        }
    }

    private static int count(java.sql.Statement stmt, String table) throws Exception {
        try(var rs = stmt.executeQuery("select count(*) from " + table)) {
            assertTrue(rs.next());
            return rs.getInt(1);
        }
    }
}
