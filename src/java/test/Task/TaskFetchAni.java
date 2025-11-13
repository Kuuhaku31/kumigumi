package Task;


import utils.TableData.TableData;

import java.io.IOException;
import java.net.URISyntaxException;

import static NetAccess.BangumiAPI.GetAnimeData;

public
class TaskFetchAni extends TaskManager.MyTask
{
    private final TableData td_anime;
    private final int       ani_id;

    public
    TaskFetchAni(TableData td_anime, int ani_id)
    {
        this.td_anime = td_anime;
        this.ani_id   = ani_id;
    }
    
    @Override
    public
    void run()
    {
        if(is_completed) return;

        try { GetAnimeData(td_anime, ani_id); }
        catch(URISyntaxException | IOException e)
        {
            System.err.println("[TaskFetchAni: ani_id:" + ani_id + "] " + e.getMessage());
        }

        is_completed = true;
    }
}
