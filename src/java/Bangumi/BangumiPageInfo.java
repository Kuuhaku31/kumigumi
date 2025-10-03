// BangumiPageInfo.java

package Bangumi;

import utils.AnimeInfo;
import utils.EpisodeInfo;

import java.util.List;

public
class BangumiPageInfo
{
    public AnimeInfo animeInfo;
    public List<EpisodeInfo> episodeList;

    public
    BangumiPageInfo(AnimeInfo animeInfo, List<EpisodeInfo> episodeList)
    {
        this.animeInfo = animeInfo;
        this.episodeList = episodeList;
    }

    public
    void PrintInfo()
    {
        IO.println("=================================");
        IO.println("Anime:");
        animeInfo.PrintInfo();

        IO.println("=================================");
        IO.println("Episodes:");
        for(EpisodeInfo ep : episodeList)
        {
            IO.println("---------------------------------");
            ep.PrintInfo();
        }
    }
}
