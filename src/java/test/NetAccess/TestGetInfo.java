package NetAccess;

public class TestGetInfo {
    public static void main(String[] args) {

        final Integer anime_id = 377130;
        final String url_rss = "https://mikan.tangbai.cc/RSS/Bangumi?bangumiId=3899";
        final String url_rss2 = "https://nyaa.si/?page=rss&q=Tongari+Boushi+no+Atelier&c=0_0&f=0";

        // try {
        //     var info = NetAccess.FetchAnimeInfo(anime_id);
        //     System.out.println(info);
        // } catch(Exception e) {
        //     e.printStackTrace();
        // }


        // try {
        //     var info = NetAccess.FetchEpisodeInfoSet(anime_id);
        //     for(var epi : info) {
        //         System.out.println(epi.toFormatString());
        //         // System.out.println(epi);
        //     }
        // } catch(Exception e) {
        //     e.printStackTrace();
        // }

        try {
            var info = NetAccess.FetchTorrentPageInfoSet(url_rss2);
            for(var tor : info) {
                System.out.println(tor.toFormatString());
                // System.out.println(tor);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
