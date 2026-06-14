package Task;

import java.util.Map;
import java.util.Set;

import Database.Info.TorrentPageInfo;
import NetAccess.NetAccess;


public class FetchTorrentPageTask extends Task {

    final String                 URL_RSS;
    private Set<TorrentPageInfo> result_set = null;

    public FetchTorrentPageTask(String URL_RSS) {
        this.URL_RSS = URL_RSS;
    }

    @Override
    public void execute() {
        start();

        try {
            result_set = NetAccess.FetchTorrentPageInfoSet(URL_RSS);
            if(result_set == null) throw new Exception("获取 TorrentPageInfo 失败");
            complete();
        } catch(Exception _) {
            fail();
        }
    }

    public Set<TorrentPageInfo> getResultSet() {
        return result_set;
    }

    @Override
    public Map<String, Object> getInfo() {
        var info = super.getInfo();
        info.put("URL_RSS", URL_RSS);
        info.put("ResultSize", result_set == null ? 0 : result_set.size());
        return info;
    }
}
