package Main;

import NetAccess.NetAccess;


class NetAccessTest {

    public static void main(String[] args) {

        System.out.println("Testing FetchAnimeInfo and FetchEpisodeInfoSet with Bangumi Anime ID 533027...");
        var id = 533027;

        try {
            var anime_info   = NetAccess.FetchAnimeInfo(id);
            var episode_info = NetAccess.FetchEpisodeInfoSet(id);

            System.out.println("Anime Info:");
            System.out.println("ID: " + anime_info.ANI_ID);
            System.out.println("Title: " + anime_info.title);
            System.out.println("Description: " + anime_info.description);
            System.out.println("Cover URL: " + anime_info.url_cover);
            System.out.println("Air Date: " + anime_info.air_date);

            System.out.println("\nEpisode Info:");
            for(var episode : episode_info) {
                System.out.println("Episode " + episode.ep + ": " + episode.title);
            }
        } catch(Exception _) {
            System.out.println("Failed to fetch info from Bangumi. This may be due to network issues or changes in the Bangumi API.");
        }
    }
}
