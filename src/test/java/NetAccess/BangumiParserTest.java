package NetAccess;

import static org.junit.jupiter.api.Assertions.*;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

class BangumiParserTest {

    @Test
    void parsesAnimeJsonIntoAnimeInfo() {
        var json = new JSONObject()
            .put("id", 100)
            .put("date", "2026-01-02")
            .put("name", "Original")
            .put("name_cn", "中文标题")
            .put("summary", "Summary")
            .put("eps", 12)
            .put("images", new JSONObject().put("large", "https://example.com/cover.jpg"))
            .put("infobox", new JSONArray()
                .put(new JSONObject()
                    .put("key", "别名")
                    .put("value", new JSONArray()
                        .put(new JSONObject().put("v", "Alias A"))
                        .put(new JSONObject().put("v", "Alias B"))))
                .put(new JSONObject()
                    .put("key", "官方网站")
                    .put("value", "https://example.com")));

        var anime = BangumiParser.ParseAnimeInfo(json);

        assertEquals(100, anime.ANI_ID);
        assertEquals("Original", anime.title);
        assertEquals("中文标题", anime.title_cn);
        assertEquals("Alias A;Alias B", anime.aliases);
        assertEquals(12, anime.episode_count);
        assertEquals("https://example.com", anime.url_official_site);
    }

    @Test
    void parsesEpisodeJsonIntoEpisodeInfo() {
        var json = new JSONObject()
            .put("subject_id", 100)
            .put("id", 200)
            .put("airdate", "2026-01-03")
            .put("ep", 1)
            .put("sort", 1.5)
            .put("name", "Episode")
            .put("name_cn", "第一话")
            .put("duration_seconds", 1440)
            .put("desc", "Description");

        var episode = BangumiParser.ParseEpisodeInfo(json);

        assertEquals(200, episode.EPI_ID);
        assertEquals(100, episode.ANI_ID);
        assertEquals(1, episode.ep);
        assertEquals(1.5, episode.sort);
        assertEquals(1440, episode.duration);
        assertEquals("Episode", episode.title);
    }
}
