// Task.java

package utils;

import java.io.IOException;
import java.net.URISyntaxException;

import static NetAccess.BangumiAPI.GetAnimeData;
import static NetAccess.BangumiAPI.GetEpisodeData;
import static NetAccess.MikanRSS.GetTorrentData;

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
    void Run()
    {
        try
        {
            // 获取 anime_info, episode_list, torrent_info_list
            anime_info_list   = GetAnimeData(ani_id);
            episode_info_list = GetEpisodeData(ani_id);
            torrent_info_list = GetTorrentData(rss_url, ani_id);
        }
        catch(URISyntaxException | IOException e)
        {
            IO.println("发生异常: " + e.getMessage());
        }
    }


    // 打印任务信息
    public
    String toString()
    {
        StringBuilder builder = new StringBuilder();

        String header      = "======" + this + "======";
        int    header_long = header.length();

        builder.append(header).append("\n");

        builder.append("ani_id: ").append(ani_id).append("\n");
        builder.append("rss_url: ").append(rss_url).append("\n");

        for(int i = 0; i < header_long; i++) IO.print("-");
        builder.append("\n");

        // 打印三个数组
        for(var strings : anime_info_list)
        {
            builder.append(strings).append("\t");
            builder.append("\n");
        }

        for(var strings : episode_info_list)
        {
            for(String string : strings) builder.append(string).append("\t");
            builder.append("\n");
        }

        for(var strings : torrent_info_list)
        {
            for(String string : strings) builder.append(string).append("\t");
            builder.append("\n");
        }

        return builder.toString();
    }


}
