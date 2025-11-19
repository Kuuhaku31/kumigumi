package Task;


import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import static NetAccess.NetAccess.FetchEpisodeInfo;


public
class TaskFetchEpi extends TaskFetch
{
    private final int ani_id;

    public
    TaskFetchEpi(List<Map<String, String>> buffer, int ani_id)
    {
        super(buffer);
        this.ani_id = ani_id;
    }

    // 执行任务的具体逻辑
    @Override
    public
    void run()
    {
        try { buffer.addAll(FetchEpisodeInfo(ani_id)); }
        catch(URISyntaxException | IOException e)
        {
            failed("[TaskFetchEpi: ani_id:" + ani_id + "] " + e.getMessage());
        }
    }

    @Override
    protected
    String getStatusStr()
    { return " ani_id=" + ani_id; }
}
