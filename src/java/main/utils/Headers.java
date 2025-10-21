// Headers.java

package utils;

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

}
