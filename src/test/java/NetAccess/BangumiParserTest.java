package NetAccess;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class BangumiParserTest {

    final String json_str_1 =
    """
    {
        "id": 100,
        "date": "2026-01-02",
        "name": "Original",
        "name_cn": "中文标题",
        "summary": "Summary",
        "eps": 12,
        "images": {
        "large": "https://example.com/cover.jpg"
        },
        "infobox": [
        {
            "key": "别名",
            "value": [
            {"v": "Alias A"},
            {"v": "Alias B"}
            ]
        },
        {
            "key": "官方网站",
            "value": "https://example.com"
        }
        ]
    }
    """;

    final String json_str_2 =
    """
    {
        "subject_id": 100,
        "id": 200,
        "airdate": "2026-01-03",
        "ep": 1,
        "sort": 1.5,
        "name": "Episode",
        "name_cn": "第一话",
        "duration_seconds": 1440,
        "desc": "Description"
    }
    """;


    @Test
    void parsesAnimeJsonIntoAnimeInfo() {

        var json  = new JSONObject(json_str_1);
        var anime = BangumiParser.parseAnimeInfo(json);

        assertEquals(100, anime.ANI_ID);
        assertEquals("Original", anime.title);
        assertEquals("中文标题", anime.title_cn);
        assertEquals("Alias A;Alias B", anime.aliases);
        assertEquals(12, anime.episode_count);
        assertEquals("https://example.com", anime.url_official_site);
    }

    @Test
    void parsesEpisodeJsonIntoEpisodeInfo() {

        var json    = new JSONObject(json_str_2);
        var episode = BangumiParser.parseEpisodeInfo(json);

        assertEquals(200, episode.EPI_ID);
        assertEquals(100, episode.ANI_ID);
        assertEquals(1, episode.ep);
        assertEquals(1.5, episode.sort);
        assertEquals(1440, episode.duration);
        assertEquals("Episode", episode.title);
        assertEquals("第一话", episode.title_cn);
        assertEquals("Description", episode.description);
    }
}
