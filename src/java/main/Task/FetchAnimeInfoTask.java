package Task;

import Database.AnimeInfo;
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
}
