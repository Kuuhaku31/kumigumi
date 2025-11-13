package utils.task;


import utils.TableData.TableData;

import java.io.IOException;
import java.net.URISyntaxException;

import static NetAccess.BangumiAPI.GetAnimeData;


public
class TaskFetchAni extends KGTask
{
    private final TableData td_anime;
    private final int       ani_id;

    public
    TaskFetchAni(TableData td_anime, int ani_id)
    {
        this.td_anime = td_anime;
        this.ani_id   = ani_id;
    }

    // 执行任务的具体逻辑
    @Override
    public
    void run()
    {
        try { GetAnimeData(td_anime, ani_id); }
        catch(URISyntaxException | IOException e)
        {
            System.err.println("[TaskFetchAni: ani_id:" + ani_id + "] " + e.getMessage());
        }

        is_completed = true;
    }

    @Override
    public
    String toString()
    { return "TaskFetchAni: is_completed=" + is_completed + " ani_id=" + ani_id; }
}
