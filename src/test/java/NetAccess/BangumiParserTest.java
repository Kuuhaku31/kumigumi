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
        assertNotNull(anime.air_date);
        assertEquals("Original", anime.title);
        assertEquals("中文标题", anime.title_cn);
        assertEquals("Alias A;Alias B", anime.aliases);
        assertEquals("Summary", anime.description);
        assertEquals(12, anime.episode_count);
        assertEquals("https://example.com", anime.url_official_site);
        assertEquals("https://example.com/cover.jpg", anime.url_cover);
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

    @Test
    void parsesAnimeJsonWithMissingOrBlankOptionalFieldsAsNull() {

        var json = new JSONObject(
        """
        {
            "id": 101,
            "date": "invalid-date",
            "name": "",
            "name_cn": " ",
            "summary": "",
            "infobox": [
                {
                    "key": "别名",
                    "value": "Alias Only"
                },
                {
                    "key": "官方网站",
                    "value": " "
                }
            ]
        }
        """);

        var anime = BangumiParser.parseAnimeInfo(json);

        assertEquals(101, anime.ANI_ID);
        assertNull(anime.air_date);
        assertNull(anime.title);
        assertNull(anime.title_cn);
        assertEquals("Alias Only", anime.aliases);
        assertNull(anime.description);
        assertNull(anime.episode_count);
        assertNull(anime.url_official_site);
        assertNull(anime.url_cover);
    }

    @Test
    void parsesEpisodeJsonWithMissingOptionalFieldsAsNull() {

        var json = new JSONObject(
        """
        {
            "subject_id": 100,
            "id": 201,
            "airdate": "2026-13-40",
            "name": "",
            "name_cn": " ",
            "duration_seconds": 0,
            "desc": ""
        }
        """);

        var episode = BangumiParser.parseEpisodeInfo(json);

        assertEquals(201, episode.EPI_ID);
        assertEquals(100, episode.ANI_ID);
        assertNull(episode.ep);
        assertNull(episode.sort);
        assertNull(episode.air_date);
        assertNull(episode.duration);
        assertNull(episode.title);
        assertNull(episode.title_cn);
        assertNull(episode.description);
    }

    @Test
    void rejectsNullBangumiJson() {
        assertThrows(IllegalArgumentException.class, () -> BangumiParser.parseAnimeInfo(null));
        assertThrows(IllegalArgumentException.class, () -> BangumiParser.parseEpisodeInfo(null));
    }
}
