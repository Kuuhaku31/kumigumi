package Database;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.Map;

import org.junit.jupiter.api.Test;

import Database.Info.RSSInfo;
import Database.Info.TorrentInfo;
import Database.Info.TorrentPageInfo;
import Database.Info.AnimeInfo;
import Database.Info.EpisodeInfo;
import Database.Info.EpisodeRecordInfo;

class DatabaseInfoTest {

    @Test
    void createsSchemaInfoObjectsFromMaps() {
        var anime = new AnimeInfo(Map.of(
            "ANI_ID", "100",
            "air_date", "2026-01-02",
            "title", "Original",
            "title_cn", "中文标题",
            "aliases", "Alias A;Alias B",
            "description", "Description",
            "episode_count", "12",
            "url_official_site", "https://example.com",
            "url_cover", "https://example.com/cover.jpg"
        ));

        assertEquals(100, anime.ANI_ID);
        assertEquals("Original", anime.title);
        assertEquals(12, anime.episode_count);

        var episode = new EpisodeInfo(Map.of(
            "EPI_ID", "200",
            "ANI_ID", "100",
            "ep", "1",
            "sort", "1.5",
            "air_date", "2026-01-03",
            "duration", "1440",
            "title", "Episode",
            "title_cn", "第一话",
            "description", "Episode description"
        ));

        assertEquals(200, episode.EPI_ID);
        assertEquals(100, episode.ANI_ID);
        assertEquals(1, episode.ep);
        assertEquals(1.5, episode.sort);

        var record = new EpisodeRecordInfo(Map.of(
            "EPI_ID", "200",
            "view_datetime", "2026-01-04T21:30:00+09:00",
            "rating", "5",
            "comment", "good"
        ));

        assertEquals(200, record.EPI_ID);
        assertEquals(OffsetDateTime.parse("2026-01-04T21:30:00+09:00"), record.view_datetime);
        assertEquals(5, record.rating);

        var rss = new RSSInfo(Map.of(
            "URL_RSS", "https://example.com/feed.xml",
            "ANI_ID", "100"
        ));

        assertEquals("https://example.com/feed.xml", rss.URL_RSS);
        assertEquals(100, rss.ANI_ID);

        var torrentPage = new TorrentPageInfo(Map.of(
            "URL_RSS", "https://example.com/feed.xml",
            "TOR_HASH", "abc123",
            "air_datetime", "2026-01-05T12:00:00+09:00",
            "url_download", "https://example.com/a.torrent",
            "url_page", "https://example.com/page",
            "title", "Torrent title",
            "subtitle_group", "Group",
            "description", "Torrent description"
        ));

        assertEquals("abc123", torrentPage.TOR_HASH);
        assertEquals("Group", torrentPage.subtitle_group);
    }

    @Test
    void createsTorrentInfoFromBencodedTorrent() throws Exception {
        var data = sampleTorrent();
        var info = new TorrentInfo(data);

        assertEquals(expectedInfoHash(), info.TOR_HASH);
        assertEquals("test.mkv", info.file_name);
        assertEquals(123L, info.file_size);
        assertArrayEquals(data, info.torrent_file);
    }

    static byte[] sampleTorrent() {
        return ("d4:info" + sampleInfoDictionary() + "e").getBytes(StandardCharsets.UTF_8);
    }

    static String expectedInfoHash() throws Exception {
        var digest = MessageDigest.getInstance("SHA-1");
        return HexFormat.of().formatHex(digest.digest(sampleInfoDictionary().getBytes(StandardCharsets.UTF_8)));
    }

    private static String sampleInfoDictionary() {
        return "d6:lengthi123e4:name8:test.mkv12:piece lengthi16384e6:pieces20:aaaaaaaaaaaaaaaaaaaae";
    }
}
