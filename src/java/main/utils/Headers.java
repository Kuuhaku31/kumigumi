// Headers.java

package utils;

import java.util.Map;
import java.util.Set;

public
class Headers
{
    // 表头
    public static final String[] ANIME_HEADERS_SRC = {
        "ANI_ID", "air_date", "title", "title_cn", "aliases", "episode_count", "url_official_site", "url_cover"
    };

    public static final String[] EPISODE_HEADERS_SRC = {
        "EPI_ID", "ANI_ID", "air_date", "duration", "index", "title", "title_cn", "description"
    };

    public static final String[] TORRENT_HEADERS_SRC = {
        "TOR_URL", "ANI_ID", "air_datetime", "size", "url_page", "title", "subtitle_group", "description"
    };

    // 允许的字段
    public static final Set<String>              ALLOWED_TABLES  = Set.of("anime", "episode", "torrent");
    public static final Map<String, Set<String>> ALLOWED_COLUMNS = Map.of(
        "anime", Set.of("ANI_ID", "air_date", "title", "title_cn", "aliases", "episode_count", "url_official_site", "url_cover"),
        "episode", Set.of("EPI_ID", "ANI_ID", "air_date", "duration", "index", "title", "title_cn", "description"),
        "torrent", Set.of("TOR_URL", "ANI_ID", "air_datetime", "size", "url_page", "subtitle_group", "description")
    );

    public
    enum TableName
    {
        anime, episode, torrent
    }
}
