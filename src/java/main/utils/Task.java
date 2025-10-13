// Task.java

package utils;

import Database.MySQL;
import NetAccess.BangumiAPI;
import NetAccess.MikanRSS;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public
class Task
{
    // 参数
    private final int ani_id;
    private final String rss_url;

    // 结果
    private AnimeInfo anime_info = null;
    private ArrayList<EpisodeInfo> episode_list = null;
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
        try
        {
            BangumiInfoSet bangumi_info_set = BangumiAPI.GetBangumiInfoSet(ani_id);
            anime_info = bangumi_info_set.anime_info;
            episode_list = bangumi_info_set.episode_info_list;
            torrent_info_list = MikanRSS.GetTorrentInfoList(rss_url, ani_id);
        }
        catch(URISyntaxException | IOException e)
        {
            IO.println("发生异常: " + e.getMessage());
        }

    }

    public
    void UpsertToDB()
    {
        try
        {
            MySQL mysql = new MySQL();
            mysql.Open();
            mysql.Upsert(anime_info);
            mysql.Close();
        }
        catch(SQLException e)
        {
            // IO.println(e.getMessage());
            // 打印详细信息
            e.printStackTrace();
        }
    }

    // 打印任务信息
    public
    void PrintInfo()
    {
        IO.println("ani_id: " + ani_id);
        IO.println("rss_url: " + rss_url);

        if(anime_info != null)
        {
            anime_info.PrintInfo();
        }
        else IO.println("Bangumi info not fetched yet.");

        if(episode_list != null)
        {
            IO.println("Episode List:");
            for(var episode : episode_list)
            {
                episode.PrintInfo();
                System.out.println("-------------------------");
            }
        }
        else IO.println("Episode info not fetched yet.");

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

    // 打印任务信息（简短）
    public
    void PrintInfoShort()
    {
        IO.println("ani_id: " + ani_id);
        IO.println("rss_url: " + rss_url);

        // 打印番剧信息
        if(anime_info != null) anime_info.PrintInfo();
        else IO.println("Bangumi info not fetched yet.");
        IO.println("==========================");

        // 打印剧集列表
        printListInfo("Episode", episode_list);
        IO.println("==========================");

        // 打印种子列表
        printListInfo("Torrent", torrent_info_list);
    }


    // 通用的列表打印逻辑（限制前3项）
    private
    <T> void printListInfo(String title, List<T> list)
    {
        // 如果列表为空，打印提示信息
        if(list == null || list.isEmpty())
        {
            IO.println(title + " info not fetched yet.");
            return;
        }

        IO.println(title + " List:");

        int limit = Math.min(list.size(), 3);
        for(int i = 0; i < limit; i++)
        {
            var item = list.get(i);
            try
            {
                // 假设每个对象都有 PrintInfo() 方法
                item.getClass().getMethod("PrintInfo").invoke(item);
            }
            catch(Exception e)
            {
                IO.println("[Error] Unable to print " + title + " info: " + e.getMessage());
            }
            System.out.println("-------------------------");
        }

        if(list.size() > 3)
        {
            IO.println("... and more ...");
        }

    }


}
