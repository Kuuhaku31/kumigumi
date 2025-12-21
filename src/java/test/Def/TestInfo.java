package Def;

import Database.InfoItem.InfoAni.InfoAniFetch;
import Database.InfoItem.InfoAni.InfoAniStore;
import Database.InfoItem.InfoEpi.InfoEpiFetch;
import Database.InfoItem.InfoEpi.InfoEpiStore;
import Database.InfoItem.InfoTor.InfoTorFetch;
import Database.InfoItem.InfoTor.InfoTorStore;

public class TestInfo {

    static final Integer TEST_ANI_ID = 143;
    public static final Integer TEST_EPI_ID = 7776;
    static final String TEST_TOR_URL = "https://example.com/testtor.torrent";

    public static InfoAniFetch infoAniFetch = new InfoAniFetch(TEST_ANI_ID);
    public static InfoAniStore infoAniStore = new InfoAniStore(TEST_ANI_ID);
    public static InfoEpiFetch infoEpiFetch = new InfoEpiFetch(TEST_EPI_ID);
    public static InfoEpiStore infoEpiStore = new InfoEpiStore(TEST_EPI_ID);
    public static InfoTorFetch infoTorFetch = new InfoTorFetch(TEST_TOR_URL);
    public static InfoTorStore infoTorStore = new InfoTorStore(TEST_TOR_URL);

    static {
        infoAniFetch.air_date = new java.util.Date();
        infoAniFetch.title = "Test Title";
        infoAniFetch.title_cn = "测试标题";
        infoAniFetch.aliases = "Alias1; Alias2";
        infoAniFetch.description = "This is a test description.";
        infoAniFetch.episode_count = 12;

        infoAniStore.url_rss = "https://example.com/rss";
        infoAniStore.rating_before = 7;
        infoAniStore.rating_after = 9;
        infoAniStore.remark = "Great anime!";

        infoEpiFetch.ANI_ID = TEST_ANI_ID;
        infoEpiFetch.ep = 1;
        infoEpiFetch.sort = 1.2f;
        infoEpiFetch.air_date = new java.util.Date();
        infoEpiFetch.duration = 24;
        infoEpiFetch.title = "Episode 1";
        infoEpiFetch.title_cn = "第一集";
        infoEpiFetch.description = "This is the first episode.";

        infoEpiStore.rating = 8;
        infoEpiStore.view_datetime = java.time.OffsetDateTime.now();
        infoEpiStore.status_download = "Completed";
        infoEpiStore.status_view = "Watched";
        infoEpiStore.remark = "Awesome episode!";

        infoTorFetch.ANI_ID = TEST_ANI_ID;
        infoTorFetch.air_datetime = java.time.OffsetDateTime.now();
        infoTorFetch.size = 1500L;
        infoTorFetch.url_page = "https://example.com/torpage";
        infoTorFetch.title = "Example Torrent";
        infoTorFetch.subtitle_group = "Example Group";
        infoTorFetch.description = "This is an example torrent description.";

        infoTorStore.status_download = "Downloaded";
        infoTorStore.remark = "Good quality torrent.";
    }

    public static void main(String[] args) {
        System.out.println(infoAniFetch);
        System.out.println(infoAniStore);
        System.out.println(infoEpiFetch);
        System.out.println(infoEpiStore);
        System.out.println(infoTorFetch);
        System.out.println(infoTorStore);
    }
}
