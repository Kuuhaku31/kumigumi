package utils.task;


import NetAccess.MikanRSS;
import NetAccess.NyaaRSS;
import utils.TableData.TableData;

import java.io.IOException;


// 获取种子信息的任务
public
class TaskFetchTor extends KGTask
{
    private final TableData td_torrent;
    private final int       ani_id;
    private final String    rss_url;

    public
    TaskFetchTor(TableData td_torrent, int ani_id, String rss_url)
    {
        this.td_torrent = td_torrent;
        this.ani_id     = ani_id;
        this.rss_url    = rss_url;
    }

    // 执行任务的具体逻辑
    @Override
    public
    void run()
    {
        if(rss_url == null) return;
        try
        {
            if(rss_url.startsWith("https://mikanani.me")) MikanRSS.GetTorrentData(td_torrent, rss_url, ani_id);
            else if(rss_url.startsWith("https://nyaa")) NyaaRSS.GetTorrentData(td_torrent, rss_url, ani_id);
            else System.err.println("未知的RSS类型");
        }
        catch(IOException e) { System.err.println("TaskFetchTor 发生异常: " + e.getMessage()); }
    }
}
