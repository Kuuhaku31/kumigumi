package Main.FetchTask;

import java.util.Map;
import java.util.Set;

import Database.Info.BaseInfo;
import Database.Info.TorrentPageInfo;
import NetAccess.NetAccess;
import Utils.DataBlock;


public class FetchTorrentPageTask extends FetchInfoTask {

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

    @Override
    public Set<? extends BaseInfo> GetInfoSet() {
        if(result_set == null) return java.util.Set.of();
        return result_set;
    }

    public static Set<FetchTorrentPageTask> ParseFetchTorrentPageTaskByDataBlock(DataBlock dataBlock) {

        var url_rss_index = dataBlock.GetColumnIndex("URL_RSS");

        Set<FetchTorrentPageTask> taskSet = new java.util.HashSet<>();
        for(var rowIndex = 0; rowIndex < dataBlock.GetRowSize(); rowIndex++) {
            String url_rss = null;
            var    row     = dataBlock.GetRow(rowIndex);

            if(url_rss_index != -1) {
                var url_rss_str = row[url_rss_index];
                if(url_rss_str != null && !url_rss_str.isBlank()) url_rss = url_rss_str.trim();
            }

            if(url_rss != null) taskSet.add(new FetchTorrentPageTask(url_rss));
        }
        return taskSet;
    }
}
