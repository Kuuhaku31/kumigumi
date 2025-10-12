// BangumiInfoSet.java

package utils;

import java.util.ArrayList;

public
class BangumiInfoSet
{
    public AnimeInfo anime_info;
    public ArrayList<EpisodeInfo> episode_info_list = new ArrayList<>();

    public
    void PrintInfo()
    {
        IO.println("=== BangumiInfoSet ===");
        anime_info.PrintInfo();
        for(EpisodeInfo ep : episode_info_list)
        {
            IO.println("==================");
            ep.PrintInfo();
        }
    }
}
