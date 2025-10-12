// InfoSet.java

package utils;

import java.util.List;

public
class InfoSet
{
    public AnimeInfo anime_info;
    public List<EpisodeInfo> episode_info_list;
    public List<TorrentInfo> torrent_info_list;

    public
    void Translate()
    {
        // anime_info.Translate();
        for(EpisodeInfo ep : episode_info_list)
        {
            // ep.Translate();
        }
        for(TorrentInfo ti : torrent_info_list)
        {
            // ti.Translate();
        }
    }

    public
    void PrintInfo()
    {
        System.out.println("=================================");
        System.out.println("Anime:");
        anime_info.PrintInfo();

        System.out.println("=================================");
        System.out.println("Episodes:");
        for(EpisodeInfo ep : episode_info_list)
        {
            System.out.println("---------------------------------");
            ep.PrintInfo();
        }

        System.out.println("=================================");
        System.out.println("Torrents:");
        for(TorrentInfo ti : torrent_info_list)
        {
            System.out.println("---------------------------------");
            ti.PrintInfo();
        }
    }
}
