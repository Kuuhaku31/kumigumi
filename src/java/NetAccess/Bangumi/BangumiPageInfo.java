// BangumiPageInfo.java

package NetAccess.Bangumi;

import utils.AnimeInfo;
import utils.EpisodeInfo;

import java.util.List;

public
class BangumiPageInfo
{
    public AnimeInfo animeInfo;
    public List<EpisodeInfo> episodeInfoList;

    public
    BangumiPageInfo(AnimeInfo animeInfo, List<EpisodeInfo> episodeList)
    {
        this.animeInfo = animeInfo;
        this.episodeInfoList = episodeList;
    }

    public
    void PrintInfo()
    {
        IO.println("=================================");
        IO.println("Anime:");
        animeInfo.PrintInfo();

        IO.println("=================================");
        IO.println("Episodes:");
        for(EpisodeInfo ep : episodeInfoList)
        {
            IO.println("---------------------------------");
            ep.PrintInfo();
        }
    }
}
