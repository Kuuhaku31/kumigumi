// Def.TestTableData.java


import Database.KG_SQLiteAccess;
import NetAccess.BangumiAPI;
import NetAccess.MikanRSS;
import utils.TableData.TableData;


void main() throws URISyntaxException, IOException
{
    // 表头
    String[] ANIME_HEADERS_SRC   = {"ANI_ID", "air_date", "title", "title_cn", "aliases", "description", "episode_count", "url_official_site", "url_cover"};
    String[] EPISODE_HEADERS_SRC = {"EPI_ID", "ANI_ID", "sort", "air_date", "duration", "ep", "title", "title_cn", "description"};
    String[] TORRENT_HEADERS_SRC = {"TOR_URL", "ANI_ID", "air_datetime", "size", "url_page", "title", "subtitle_group", "description"};


    System.out.println("Def.TestTableData");

    TableData ani = new TableData(ANIME_HEADERS_SRC);
    TableData epi = new TableData(EPISODE_HEADERS_SRC);
    TableData tor = new TableData(TORRENT_HEADERS_SRC);

    BangumiAPI.GetAnimeData(ani, 507634);
    BangumiAPI.GetEpisodeData(epi, 507634);
    MikanRSS.GetTorrentData(tor, "https://mikanani.me/RSS/Bangumi?bangumiId=3774", 507634);

    System.out.println(ani);

    KG_SQLiteAccess dba = new KG_SQLiteAccess();

    dba.Open();
    dba.Upsert(KG_SQLiteAccess.TableName.anime, ani);
    dba.Upsert(KG_SQLiteAccess.TableName.episode, epi);
    dba.Upsert(KG_SQLiteAccess.TableName.torrent, tor);
    dba.Close();
}
