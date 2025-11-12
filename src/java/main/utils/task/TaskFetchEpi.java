package utils.task;


import utils.TableData.TableData;

import java.io.IOException;
import java.net.URISyntaxException;

import static NetAccess.BangumiAPI.GetEpisodeData;


public
class TaskFetchEpi extends KGTask
{
    private final TableData td_episode;
    private final int       ani_id;

    public
    TaskFetchEpi(TableData td_episode, int ani_id)
    {
        this.td_episode = td_episode;
        this.ani_id     = ani_id;
    }

    // 执行任务的具体逻辑
    @Override
    public
    void run()
    {
        try { GetEpisodeData(td_episode, ani_id); }
        catch(URISyntaxException | IOException e)
        {
            System.err.println("[TaskFetchEpi: ani_id:" + ani_id + "] " + e.getMessage());
        }
    }
}
