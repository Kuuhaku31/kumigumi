package Task;

import java.io.IOException;
import java.net.URISyntaxException;

import static NetAccess.NetAccess.FetchEpisodeInfo;

public class TaskFetchEpi extends TaskFetch {
    private final int ani_id;

    public TaskFetchEpi(int ani_id) {
        this.ani_id = ani_id;
    }

    // 执行任务的具体逻辑
    @Override
    public void run() {
        try {
            buffer.addAll(FetchEpisodeInfo(ani_id));
            completed("Epi fetch finished");
        } catch (URISyntaxException | IOException e) {
            addLog("[TaskFetchEpi: ani_id:" + ani_id + "] " + e.getMessage());
        }
    }

    @Override
    protected String getStatusStr() {
        return " ani_id=" + ani_id;
    }
}
