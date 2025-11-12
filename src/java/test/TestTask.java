import utils.TableData.TableData;
import utils.task.KGTask;
import utils.task.TaskFetchTor;


void main() throws IOException
{
    final String[] ANIME_HEADERS_FETCH = new String[] {
        "ANI_ID",
        "air_date",
        "title",
        "title_cn",
        "aliases",
        "description",
        "episode_count",
        "url_official_site",
        "url_cover",
    };
    TableData ani = new TableData(ANIME_HEADERS_FETCH);

    final String[] TORRENT_HEADERS_FETCH = new String[] {
        "TOR_URL",
        "ANI_ID",
        "air_datetime",
        "size",
        "url_page",
        "title",
        "subtitle_group",
        "description",
    };
    TableData tor = new TableData(TORRENT_HEADERS_FETCH);

    // KGTask fa = new TaskFetchAni(ani, 507634);
    //
    // fa.run();
    //
    // System.out.println(ani);

    KGTask ft = new TaskFetchTor(tor, 507634, "https://mikanani.me/RSS/Bangumi?bangumiId=3774");

    ft.run();
    // GetTorrentData(tor, "https://mikanani.me/RSS/Bangumi?bangumiId=3774", 507634);

    System.out.println(tor);
}
