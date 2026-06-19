package Database;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import Info.RSSInfo;
import Info.TorrentInfo;
import Info.TorrentPageInfo;
import Utils.CreateDatabaseViews;
import Info.AnimeInfo;
import Info.EpisodeInfo;
import Info.EpisodeRecordInfo;
import Info.InfoTest;

class SQLiteAccessTest {

    @TempDir
    Path tempDir;

    @Test
    void initializesDatabaseAndUpsertsCurrentInfoTypes() throws Exception {
        var dbPath = tempDir.resolve("kumigumi-test.db").toString();
        var torrent = new TorrentInfo(InfoTest.sampleTorrent());

        try(var db = new SQLiteAccess(dbPath)) {
            db.UpsertInfo(Set.of(
                new AnimeInfo(Map.of(
                    "ANI_ID", "100",
                    "title", "Anime",
                    "episode_count", "12"
                )),
                new EpisodeInfo(Map.of(
                    "EPI_ID", "200",
                    "ANI_ID", "100",
                    "ep", "1",
                    "sort", "1",
                    "duration", "1440",
                    "title", "Episode"
                )),
                new EpisodeRecordInfo(
                    200,
                    OffsetDateTime.parse("2026-01-04T21:30:00+09:00"),
                    5,
                    "watched"
                ),
                new RSSInfo("https://example.com/feed.xml", 100),
                new TorrentPageInfo(
                    "https://example.com/feed.xml",
                    torrent.TOR_HASH,
                    OffsetDateTime.parse("2026-01-05T12:00:00+09:00"),
                    "https://example.com/download.torrent",
                    "https://example.com/page",
                    "Title",
                    "Group",
                    "Description"
                ),
                torrent
            ));

            assertEquals(Set.of("missing"), db.GetTorrentHashNotExist(Set.of(torrent.TOR_HASH, "missing")));

            var downloaders = db.GetDownloaderByHash(Set.of(torrent.TOR_HASH));
            assertEquals(1, downloaders.size());
            var downloader = downloaders.iterator().next();
            assertEquals(torrent.TOR_HASH, downloader.TOR_HASH());
            assertEquals(List.of("https://example.com/download.torrent"), downloader.url_download_list());

            db.ReplaceRequiredAnimeIds(Set.of());
            db.ReplaceRequiredAnimeIds(Set.of(999));
        }

        try(var conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            var stmt = conn.createStatement()) {
            assertEquals(0, count(stmt, "view_anime"));
            assertEquals(0, count(stmt, "view_episode"));
            assertEquals(0, count(stmt, "view_torrent_page"));
        }

        var aniIdFile = tempDir.resolve("ani-ids.txt");
        Files.writeString(aniIdFile, "100, 100;\n100");
        assertEquals(Set.of(100), CreateDatabaseViews.ReadAnimeIds(aniIdFile.toString()));
        CreateDatabaseViews.Create(dbPath, aniIdFile.toString());

        try(var conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            var stmt = conn.createStatement()) {
            assertEquals(1, count(stmt, "anime"));
            assertEquals(1, count(stmt, "episode"));
            assertEquals(1, count(stmt, "episode_record"));
            assertEquals(1, count(stmt, "rss"));
            assertEquals(1, count(stmt, "torrent_page"));
            assertEquals(1, count(stmt, "torrent"));
            assertEquals(1, count(stmt, "required_anime_id"));

            try(var rs = stmt.executeQuery("SELECT * FROM view_anime WHERE ANI_ID = 100")) {
                assertTrue(rs.next());
                assertEquals("Anime", rs.getString("ani_title"));
                assertEquals("https://example.com/feed.xml", rs.getString("ani_rss_list"));
                assertEquals("https://bgm.tv/subject/100", rs.getString("ani_bgm_site"));
            }

            try(var rs = stmt.executeQuery("SELECT * FROM view_episode WHERE EPI_ID = 200")) {
                assertTrue(rs.next());
                assertEquals("Episode", rs.getString("epi_title"));
                assertEquals("Anime", rs.getString("ani_title"));
            }

            try(var rs = stmt.executeQuery("SELECT * FROM view_torrent_page WHERE TOR_HASH = '" + torrent.TOR_HASH + "'")) {
                assertTrue(rs.next());
                assertEquals("Title", rs.getString("title"));
                assertEquals("Anime", rs.getString("ani_title"));
                assertEquals(torrent.file_name, rs.getString("tor_file_name"));
                assertEquals(torrent.file_size, rs.getObject("tor_file_size", Long.class));
            }
        }

        Files.writeString(aniIdFile, "999");
        CreateDatabaseViews.Create(dbPath, aniIdFile.toString());
        try(var conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            var stmt = conn.createStatement()) {
            assertEquals(1, count(stmt, "required_anime_id"));
            assertEquals(0, count(stmt, "view_anime"));
            assertEquals(0, count(stmt, "view_episode"));
            assertEquals(0, count(stmt, "view_torrent_page"));
        }
    }

    @Test
    void printsFailingInfoAndRollsBackWhenUpsertFails() throws Exception {
        var dbPath = tempDir.resolve("kumigumi-fail-test.db").toString();
        var errBuffer = new ByteArrayOutputStream();
        var prevErr = System.err;

        System.setErr(new PrintStream(errBuffer, true, StandardCharsets.UTF_8));
        try {
            try(var db = new SQLiteAccess(dbPath)) {
                var goodAnime = new AnimeInfo(Map.of(
                    "ANI_ID", "100",
                    "title", "Will be rolled back"
                ));
                var badEpisode = new EpisodeInfo(Map.of(
                    "EPI_ID", "999",
                    "ANI_ID", "404",
                    "title", "Missing anime"
                ));

                assertThrows(SQLException.class, () -> db.UpsertInfo(Set.of(goodAnime, badEpisode)));
            }
        } finally {
            System.setErr(prevErr);
        }

        var errOutput = errBuffer.toString(StandardCharsets.UTF_8);
        assertTrue(errOutput.contains("数据库写入失败"));
        assertTrue(errOutput.contains("问题数据项 #1: EpisodeInfo"));
        assertTrue(errOutput.contains("EPI_ID:\t999"));
        assertTrue(errOutput.contains("ANI_ID:\t404"));

        try(var conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            var stmt = conn.createStatement()) {
            assertEquals(0, count(stmt, "anime"));
            assertEquals(0, count(stmt, "episode"));
        }
    }

    @Test
    void rejectsExistingDatabaseWithMissingView() throws Exception {
        var dbPath = tempDir.resolve("missing-view.db").toString();
        try(var _ = new SQLiteAccess(dbPath)) {}

        try(var conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            var stmt = conn.createStatement()) {
            stmt.execute("DROP VIEW view_episode");
        }

        var error = assertThrows(SQLException.class, () -> new SQLiteAccess(dbPath));
        assertTrue(error.getMessage().contains("缺失 view: view_episode"));
    }

    @Test
    void rejectsExistingDatabaseWithIncorrectTableStructure() throws Exception {
        var dbPath = tempDir.resolve("incorrect-table.db").toString();
        try(var _ = new SQLiteAccess(dbPath)) {}

        try(var conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            var stmt = conn.createStatement()) {
            stmt.execute("ALTER TABLE anime ADD COLUMN unexpected text");
        }

        var error = assertThrows(SQLException.class, () -> new SQLiteAccess(dbPath));
        assertTrue(error.getMessage().contains("结构不正确 table: anime"));
    }

    private static int count(java.sql.Statement stmt, String table) throws Exception {
        try(var rs = stmt.executeQuery("select count(*) from " + table)) {
            assertTrue(rs.next());
            return rs.getInt(1);
        }
    }
}
