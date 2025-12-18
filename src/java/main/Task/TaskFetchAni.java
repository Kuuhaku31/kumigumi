package Task;

import java.io.IOException;
import java.net.URISyntaxException;

import static NetAccess.NetAccess.FetchAnimeInfo;

public class TaskFetchAni extends TaskFetch {
    private final int ani_id;

    public TaskFetchAni(int ani_id) {
        this.ani_id = ani_id;
    }

    // 执行任务的具体逻辑
    @Override
    public void run() {
        try {
            buffer.add(FetchAnimeInfo(ani_id));
            completed("Ani fetch finished");
        } catch (URISyntaxException | IOException e) {
            addLog(e.getMessage());
        }
    }

    @Override
    protected String getStatusStr() {
        return " ani_id=" + ani_id;
    }
}
