// BangumiInfoSet.java

package utils;

import utils.Info.AnimeInfo;
import utils.Info.EpisodeInfo;

import java.util.ArrayList;

public
class BangumiInfoSet
{
    public AnimeInfo anime_info;
    public ArrayList<EpisodeInfo> episode_info_list = new ArrayList<>();

    public
    void PrintInfo()
    {
        if(anime_info == null)
        {
            IO.println("BangumiInfoSet is null");
            return;
        }

        IO.println("=== BangumiInfoSet ===");
        anime_info.PrintInfo();
        for(EpisodeInfo ep : episode_info_list)
        {
            IO.println("==================");
            ep.PrintInfo();
        }
    }
}
