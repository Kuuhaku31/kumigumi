package Task;

import java.util.Map;

import Database.Info.AnimeInfo;
import NetAccess.NetAccess;


public class FetchAnimeInfoTask extends Task {

    final Integer     ANI_ID;
    private AnimeInfo result = null;

    public FetchAnimeInfoTask(Integer ANI_ID) {
        this.ANI_ID = ANI_ID;
    }

    public void execute() {
        start();

        try {
            result = NetAccess.FetchAnimeInfo(ANI_ID);
            if(result == null) throw new Exception("获取 AnimeInfo 失败");
            complete();
        } catch(Exception _) {
            fail();
        }
    }

    public AnimeInfo getResult() {
        return result;
    }

    @Override
    public Map<String, Object> getInfo() {
        var info = super.getInfo();
        info.put("ANI_ID", ANI_ID);
        info.put("Result", result);
        return info;
    }
}
