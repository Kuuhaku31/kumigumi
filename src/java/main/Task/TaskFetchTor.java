package Task;


import java.io.IOException;

import static NetAccess.NetAccess.FetchTorrentInfo;


// 获取种子信息的任务
public
class TaskFetchTor extends TaskFetch
{
    private final int    ani_id;
    private final String rss_url;

    public
    TaskFetchTor(int ani_id, String rss_url)
    {
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
            var info = FetchTorrentInfo(rss_url);
            for(var i : info) i.put("ANI_ID", String.valueOf(ani_id)); // 添加 ani_id 为外键
            buffer.addAll(info);

            completed("Tor fetch finished");
        }
        catch(IOException e)
        {
            addLog(e.getMessage());
        }
    }


    @Override
    protected
    String getStatusStr()
    { return "ani_id=" + ani_id + " rss_url=" + rss_url; }
}
