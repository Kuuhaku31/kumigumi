package utils.task;


import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import static NetAccess.BangumiAPI.GetAnimeData;


public
class TaskFetchAni extends TaskFetch
{
    private final int ani_id;

    public
    TaskFetchAni(List<Map<String, String>> buffer, int ani_id)
    {
        super(buffer);
        this.ani_id = ani_id;
    }

    // 执行任务的具体逻辑
    @Override
    public
    void run()
    {
        try { buffer.add(GetAnimeData(ani_id)); }
        catch(URISyntaxException | IOException e)
        {
            failed(e.getMessage());
        }
    }

    @Override
    protected
    String getStatusStr()
    { return " ani_id=" + ani_id; }
}
