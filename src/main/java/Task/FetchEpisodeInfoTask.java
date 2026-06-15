package Task;

import java.util.Map;
import java.util.Set;

import Database.Info.EpisodeInfo;
import NetAccess.NetAccess;


public class FetchEpisodeInfoTask extends Task {

    final Integer            ANI_ID;
    private Set<EpisodeInfo> result_set = null;

    public FetchEpisodeInfoTask(Integer ANI_ID) {
        this.ANI_ID = ANI_ID;
    }

    public void execute() {
        start();

        try {
            result_set = NetAccess.FetchEpisodeInfoSet(ANI_ID);
            if(result_set == null) throw new Exception("获取 EpisodeInfo 失败");
            complete();
        } catch(Exception _) {
            fail();
        }
    }

    public Set<EpisodeInfo> getResult() {
        return result_set;
    }

    @Override
    public Map<String, Object> getInfo() {
        var info = super.getInfo();
        info.put("ANI_ID", ANI_ID);
        info.put("ResultType", result_set != null ? result_set.getClass().getSimpleName() : "null");
        info.put("ResultSize", result_set == null ? 0 : result_set.size());
        return info;
    }
}
