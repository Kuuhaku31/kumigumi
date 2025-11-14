package Database;

public
class DBStructure
{
    public static final
    Headers[] ANIME_HEADERS = {
        Headers.ANI_ID,
        Headers.air_date,
        Headers.title,
        Headers.title_cn,
        Headers.aliases,
        Headers.description,
        Headers.episode_count,
        Headers.url_official_site,
        Headers.url_cover,
        Headers.url_rss,
        Headers.rating_before,
        Headers.rating_after,
        Headers.remark
    };

    public static final
    Headers[] EPISODE_HEADERS = {
        Headers.EPI_ID,
        Headers.ANI_ID,
        Headers.sort,
        Headers.air_date,
        Headers.duration,
        Headers.ep,
        Headers.title,
        Headers.title_cn,
        Headers.description,
        Headers.rating,
        Headers.status_download,
        Headers.status_view,
        Headers.remark
    };
    
    public static final
    Headers[] TORRENT_HEADERS = {
        Headers.TOR_URL,
        Headers.ANI_ID,
        Headers.air_datetime,
        Headers.size,
        Headers.url_page,
        Headers.title,
        Headers.subtitle_group,
        Headers.description,
        Headers.status_download,
        Headers.remark
    };

    public
    enum Headers
    {
        air_date,
        air_datetime,
        aliases,
        ANI_ID,
        description,
        duration,
        ep,
        EPI_ID,
        episode_count,
        rating,
        rating_after,
        rating_before,
        remark,
        size,
        sort,
        status_download,
        status_view,
        subtitle_group,
        title,
        title_cn,
        TOR_URL,
        url_cover,
        url_official_site,
        url_page,
        url_rss,
    }

}

