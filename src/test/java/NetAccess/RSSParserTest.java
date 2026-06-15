package NetAccess;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.apptasticsoftware.rssreader.DateTimeParser;
import com.apptasticsoftware.rssreader.Enclosure;
import com.apptasticsoftware.rssreader.Item;


class RSSParserTest {

    private static final DateTimeParser TEST_DATE_TIME_PARSER = new DateTimeParser() {
        @Override
        public ZonedDateTime parse(String value) {
            return ZonedDateTime.parse("2026-01-01T00:00:00Z");
        }

        @Override
        public Instant toInstant(String value) {
            return Instant.EPOCH;
        }
    };

    @Test
    void parsesMikanItemIntoTorrentPageInfo() {

        var item      = new Item(TEST_DATE_TIME_PARSER);
        var enclosure = new Enclosure();
        enclosure.setUrl("https://mikanani.me/Download/abc123.torrent");
        item.setEnclosure(enclosure);
        item.setPubDate("2026-05-31T23:01:07.56");
        item.setLink("https://mikanani.me/Home/Episode/abc123");
        item.setTitle("[Group] Anime Title");
        item.setDescription("Mikan description");

        var info = RSSParser.parseMikanItem("https://mikanani.me/RSS/Bangumi?bangumiId=1", item);

        assertEquals("https://mikanani.me/RSS/Bangumi?bangumiId=1", info.URL_RSS);
        assertEquals("abc123", info.TOR_HASH);
        assertEquals(OffsetDateTime.parse("2026-05-31T23:01:07.56+08:00"), info.air_datetime);
        assertEquals("https://mikanani.me/Download/abc123.torrent", info.url_download);
        assertEquals("https://mikanani.me/Home/Episode/abc123", info.url_page);
        assertEquals("[Group] Anime Title", info.title);
        assertEquals("Group", info.subtitle_group);
        assertEquals("Mikan description", info.description);
    }

    @Test
    void rejectsMikanItemWithoutEnclosure() {

        var item = new Item(TEST_DATE_TIME_PARSER);
        item.setTitle("[Group] Anime Title");

        assertThrows(RuntimeException.class, () -> RSSParser.parseMikanItem("https://mikanani.me/rss", item));
    }

    @Test
    void parsesNyaaItemIntoTorrentPageInfo() {

        var item = new Item(TEST_DATE_TIME_PARSER);
        item.setPubDate("Thu, 28 May 2026 22:06:07 -0000");
        item.setLink("https://nyaa.si/download/100.torrent");
        item.setGuid("https://nyaa.si/view/100");
        item.setTitle("【Sub Group】 Anime Title");
        item.setDescription("Nyaa description");

        var extensions = Map.of(item, Map.of("infoHash", "HASH100"));
        var info       = RSSParser.parseNyaaItem("https://nyaa.si/?page=rss&q=anime", item, extensions);

        assertEquals("https://nyaa.si/?page=rss&q=anime", info.URL_RSS);
        assertEquals("HASH100", info.TOR_HASH);
        assertEquals(OffsetDateTime.parse("2026-05-28T22:06:07Z"), info.air_datetime);
        assertEquals("https://nyaa.si/download/100.torrent", info.url_download);
        assertEquals("https://nyaa.si/view/100", info.url_page);
        assertEquals("【Sub Group】 Anime Title", info.title);
        assertEquals("Sub Group", info.subtitle_group);
        assertEquals("Nyaa description", info.description);
    }

    @Test
    void rejectsNyaaItemWithoutInfoHash() {

        var item = new Item(TEST_DATE_TIME_PARSER);
        item.setTitle("Anime Title");

        assertThrows(
            IllegalArgumentException.class,
            () -> RSSParser.parseNyaaItem("https://nyaa.si/?page=rss", item, Map.of()));
    }
}
