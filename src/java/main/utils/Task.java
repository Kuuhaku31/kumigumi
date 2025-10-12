// Task.java

package utils;

import NetAccess.BangumiAPI;
import NetAccess.MikanRSS;

import java.util.ArrayList;

public
class Task
{
    // 参数
    private final int ani_id;
    private final String rss_url;

    // 结果
    private BangumiInfoSet bangumi_info_set = null;
    private ArrayList<TorrentInfo> torrent_info_list = null;

    public
    Task(int ani_id, String rss_url)
    {
        this.ani_id = ani_id;
        this.rss_url = rss_url;
    }

    // 执行任务的具体逻辑
    public
    void Run()
    {
        bangumi_info_set = BangumiAPI.BangumiInfoSet(ani_id);
        torrent_info_list = MikanRSS.GetTorrentInfoList(rss_url, ani_id);
    }

    // 打印任务信息
    public
    void PrintInfo()
    {
        IO.println("ani_id: " + ani_id);
        IO.println("rss_url: " + rss_url);

        if(bangumi_info_set != null)
        {
            bangumi_info_set.PrintInfo();
        }
        else IO.println("Bangumi info not fetched yet.");

        if(torrent_info_list != null)
        {
            for(var torrent : torrent_info_list)
            {
                torrent.PrintInfo();
                System.out.println("-------------------------");
            }
        }
        else IO.println("Torrent info not fetched yet.");
    }
}
