package Task;


import NetAccess.MikanRSS;
import NetAccess.NyaaRSS;

import java.io.IOException;
import java.util.List;
import java.util.Map;


// 获取种子信息的任务
public
class TaskFetchTor extends TaskFetch
{
    private final int    ani_id;
    private final String rss_url;

    public
    TaskFetchTor(List<Map<String, String>> buffer, int ani_id, String rss_url)
    {
        super(buffer);
        this.ani_id  = ani_id;
        this.rss_url = rss_url;
    }

    // 执行任务的具体逻辑
    @Override
    public
    void run()
    {
        try
        {
            List<Map<String, String>> torrent_data = null;

            // 不同的订阅源选择不同方法获取
            if(rss_url.startsWith("https://mikanani.me")) torrent_data = MikanRSS.GetTorrentData(rss_url, ani_id);
            else if(rss_url.startsWith("https://nyaa")) torrent_data = NyaaRSS.GetTorrentData(rss_url, ani_id);

            if(torrent_data == null) failed("不支持的RSS源");
            else buffer.addAll(torrent_data);
        }
        catch(IOException e)
        {
            failed(e.getMessage());
        }
    }


    @Override
    protected
    String getStatusStr()
    { return "ani_id=" + ani_id + " rss_url=" + rss_url; }
}
