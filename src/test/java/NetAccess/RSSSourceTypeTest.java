package NetAccess;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;


class RSSSourceTypeTest {

    @Test
    void detectsSupportedRssSourcesByUrlPrefix() {

        assertEquals(RSSSourceType.MIKAN, RSSSourceType.detectRSSSourceType("https://mikanani.me/RSS/Bangumi?bangumiId=1"));
        assertEquals(RSSSourceType.MIKAN, RSSSourceType.detectRSSSourceType("https://mikan.tangbai.cc/RSS/Bangumi?bangumiId=1"));
        assertEquals(RSSSourceType.NYAA, RSSSourceType.detectRSSSourceType("https://nyaa.si/?page=rss&q=anime"));
    }

    @Test
    void detectsRssSourceCaseInsensitively() {
        assertEquals(RSSSourceType.MIKAN, RSSSourceType.detectRSSSourceType("https://MIKANANI.ME/RSS/Bangumi?bangumiId=1"));
        assertEquals(RSSSourceType.NYAA, RSSSourceType.detectRSSSourceType("https://NYAA.si/?page=rss&q=anime"));
    }

    @Test
    void returnsUnknownForNullOrUnsupportedRssSources() {
        assertEquals(RSSSourceType.UNKNOWN, RSSSourceType.detectRSSSourceType(null));
        assertEquals(RSSSourceType.UNKNOWN, RSSSourceType.detectRSSSourceType("https://example.com/rss.xml"));
    }
}
