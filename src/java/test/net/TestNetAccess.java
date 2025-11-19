package net;

import java.io.IOException;
import java.net.URISyntaxException;

import static NetAccess.NetAccess.*;
import static util.Util.printMap;
import static util.Util.printMapList;

public
class TestNetAccess
{
    static
    void main()
    {
        var id  = 507634;
        var rss = "https://mikanani.me/RSS/Bangumi?bangumiId=3774";

        try
        {
            var ani_info = FetchAnimeInfo(id);
            var epi_info = FetchEpisodeInfo(id);
            var tor_info = FetchTorrentInfo(rss);

            printMap(ani_info);
            printMapList(epi_info);
            printMapList(tor_info);
        }
        catch(URISyntaxException | IOException e)
        {
            System.err.println("【错误】" + e.getMessage());
        }
    }
}
