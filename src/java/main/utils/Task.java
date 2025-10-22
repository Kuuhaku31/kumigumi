// Task.java

package utils;

import NetAccess.BangumiAPI;
import NetAccess.MikanRSS;

import java.io.IOException;
import java.net.URISyntaxException;

public
class Task
{
    // 参数
    private final int    ani_id;
    private final String rss_url;

    // 结果
    public String[]   anime_info_list   = new String[0];
    public String[][] episode_info_list = new String[0][];
    public String[][] torrent_info_list = new String[0][];

    public
    Task(int ani_id)
    {
        this.ani_id  = ani_id;
        this.rss_url = null;
    }

    public
    Task(int ani_id, String rss_url)
    {
        this.ani_id  = ani_id;
        this.rss_url = rss_url;
    }

    // 执行任务的具体逻辑
    public
    void GetInfo()
    {
        try
        {
            // 获取 anime_info, episode_list, torrent_info_list
            anime_info_list   = BangumiAPI.GetAnimeData(ani_id);
            episode_info_list = BangumiAPI.GetEpisodeData(ani_id);
            if(rss_url != null) torrent_info_list = MikanRSS.GetTorrentInfoList(rss_url, ani_id);
        }
        catch(URISyntaxException | IOException e)
        {
            IO.println("发生异常: " + e.getMessage());
        }
    }


    // 打印任务信息
    public
    void PrintInfo()
    {
        String header      = "======" + this + "======";
        int    header_long = header.length();

        IO.println(header);

        IO.println("ani_id: " + ani_id);
        IO.println("rss_url: " + rss_url);

        for(int i = 0; i < header_long; i++) IO.print("-");
        IO.println();

        // 打印三个数组
        for(var strings : anime_info_list)
        {
            IO.println(strings + "\t");
            IO.println();
        }

        for(var strings : episode_info_list)
        {
            for(String string : strings) IO.println(string + "\t");
            IO.println();
        }

        for(var strings : torrent_info_list)
        {
            for(String string : strings) IO.println(string + "\t");
            IO.println();
        }

    }


}
