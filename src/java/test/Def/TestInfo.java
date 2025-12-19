package Def;

import Database.InfoItem.InfoAni.InfoAniFetch;

public class TestInfo {
    public static void main(String[] args) {
        System.out.println("TestInfo");

        var infoAniFetch = new InfoAniFetch(1);
        infoAniFetch.air_date = new java.util.Date();
        System.out.println(infoAniFetch);

        var infoAniStore = new Database.InfoItem.InfoAni.InfoAniStore(1);
        infoAniStore.url_rss = "https://example.com/rss";
        infoAniStore.rating_before = 5;
        infoAniStore.rating_after = 8;
        infoAniStore.remark = "Great anime!";
        System.out.println(infoAniStore);

        var infoEpiFetch = new Database.InfoItem.InfoEpi.InfoEpiFetch(1, 1);
        infoEpiFetch.air_date = new java.util.Date();
        System.out.println(infoEpiFetch);

        var infoEpiStore = new Database.InfoItem.InfoEpi.InfoEpiStore(1, 1);
        infoEpiStore.rating = 9;
        infoEpiStore.view_datetime = java.time.LocalDateTime.now();
        infoEpiStore.status_download = "Completed";
        infoEpiStore.status_view = "Watched";
        infoEpiStore.remark = "Awesome episode!";
        System.out.println(infoEpiStore);
    }
}
