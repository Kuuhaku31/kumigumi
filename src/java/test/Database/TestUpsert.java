package Database;

import java.io.IOException;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;


public class TestUpsert {
    public static void main(String[] args) throws SQLException, IOException {

        System.out.println("Testing Upsert...");

        System.out.println("Testing Upsert AnimeInfo...");
        try (var db = new SQLiteAccess("db/test.db")) {
            var item = new AnimeInfo(Map.of(
                "ANI_ID", "1",
                "air_date", "2024-01-01",
                "title", "Test Anime",
                "title_cn", "测试动画",
                "aliases", "TA;测试动画",
                "description", "这是一个测试动画。",
                "episode_count", "112",
                "url_official_site", "https://example.com/test-anime",
                "url_cover", "https://example.com/test-anime-cover.jpg"
            ));
            db.UpsertAnimeInfo(item);
        }

        System.out.println("Testing Upsert EpisodeInfo...");
        try (var db = new SQLiteAccess("db/test.db")) {
            var item = new EpisodeInfo(Map.of(
                "EPI_ID", "1",
                "ANI_ID", "1",
                "ep", "1",
                "sort", "1.500004",
                "air_date", "2024-01-01",
                "duration", "24",
                "title", "Test Episode",
                "title_cn", "测试集",
                "description", "这是一个测试集。"
            ));
            db.UpsertEpisodeInfo(item);
        }

        System.out.println("Testing Upsert TorrentPageInfo...");
        try (var db = new SQLiteAccess("db/test.db")) {
            var item = new TorrentPageInfo(Map.of(
                "URL_RSS", "https://example.com/test-anime/rss",
                "TOR_HASH", "abc123",

                "air_datetime", "2024-01-01T12:00:00Z",
                "url_download", "https://example.com/test-anime/download",
                "url_page", "https://example.com/test-anime/page",
                "title", "Test Torrent",
                "subtitle_group", "Test Group",
                "description", "这是一个测试种子。"
            ));
            db.UpsertTorrentPageInfo(item);
        }

        System.out.println("Testing Upsert RSSInfo...");
        try (var db = new SQLiteAccess("db/test.db")) {
            var item = new RSSInfo(Map.of(
                "URL_RSS", "https://example.com/test-anime/rss",
                "ANI_ID", "1"
            ));
            // db.UpsertRSS(item);
        }

        System.out.println("Testing Upsert EpisodeRecordInfo...");
        try (var db = new SQLiteAccess("db/test.db")) {
            var item = new EpisodeRecordInfo(Map.of(
                "EPI_ID", "1",
                // "view_datetime", "2024-01-01T12:00:00Z",
                "view_datetime", OffsetDateTime.now().toString(),
                "rating", "5",
                "comment", "这是一个测试剧集记录。"
            ));
            db.UpsertEpisodeRecord(item);
        }

        System.out.println("Testing Upsert TorrentInfo...");
        try (var db = new SQLiteAccess("db/test.db")) {
            var tor_path = "ea5e686c111c47dcadbd16f335d7e7d79e48563f.torrent";

            // 读取文件内容为字节数组
            byte[] data = java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(tor_path));

            var item = new TorrentInfo(data);
            db.UpsertTorrentInfo(item);
        }

        System.out.println("Testing Export Torrent Files...");
        try (var db = new SQLiteAccess("db/test.db")) {
            Set<String> torHashList = new LinkedHashSet<>();
            torHashList.add("ea5e686c111c47dcadbd16f335d7e7d79e48563f");
            db.ExportTorrentFiles(torHashList, "./db/");

            torHashList.add("68768");
            var notExistHashes = db.GetTorrentHashNotExist(torHashList);
            System.out.println("Torrent hashes not found in database: " + notExistHashes);
        }

        System.out.println("Testing Get Torrent Files Download URLs...");
        try (var db = new SQLiteAccess("db/test.db")) {
            Set<String> torHashList = new LinkedHashSet<>();
            torHashList.add("abc123");
            var downloader_list = db.GetDownloaderByHash(torHashList);
            for (var downloader : downloader_list) {
                System.out.println("TOR_HASH: " + downloader.TOR_HASH());
                System.out.println("Download URLs: " + downloader.url_download_list());
            }
        }
    }
}
