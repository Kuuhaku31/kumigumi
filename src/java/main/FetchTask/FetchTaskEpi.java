package FetchTask;

import java.io.IOException;
import java.net.URISyntaxException;

import InfoItem.InfoEpi.InfoEpi;
import InfoItem.InfoEpi.InfoEpiFetch;
import NetAccess.NetAccess;


public class FetchTaskEpi extends FetchTask {

    // 任务参数
    final Integer ani_id;

    public FetchTaskEpi(FetchTaskManager manager, Integer ani_id) {
        super(manager);
        this.ani_id = ani_id;
    }

    @Override
    public void run() {
        try {
            var epiInfoList = NetAccess.FetchEpisodeInfo(ani_id);
            for(var epi : epiInfoList) {
                manager.bufferUpsert.add(new InfoEpi(epi));
                manager.bufferUpdate.add(new InfoEpiFetch(epi));
            }
        } catch(URISyntaxException | IOException e) {
            System.err.println("Error fetching episode info for ANI_ID=" + ani_id + ": " + e.getMessage());
        }
    }

    @Override
    public String toString() {
        return "FetchTaskEpi{ANI_ID=" + ani_id + "}";
    }
}
