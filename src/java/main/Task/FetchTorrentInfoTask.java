package Task;

import java.util.Map;

import Database.TorrentDownloader;
import NetAccess.NetAccess;


public class FetchTorrentInfoTask extends Task {

    final TorrentDownloader downloader;
    private byte[]          result = null;

    public FetchTorrentInfoTask(TorrentDownloader downloader) {
        this.downloader = downloader;
    }

    public void execute() {
        start();

        try {
            var url_list = downloader.getUrlList();
            for(var url : url_list) {
                result = NetAccess.DownloadFile(url);
                if(result != null && result.length > 0) break;
            }

            if(result == null || result.length == 0) {
                result = null;
                throw new Exception("获取 TorrentInfo 失败");
            }
            complete();

        } catch(Exception _) {
            fail();
        }
    }

    public byte[] getResult() {
        return result;
    }

    @Override
    public Map<String, Object> getInfo() {
        var info = super.getInfo();
        info.put("TOR_HASH", downloader);
        info.put("ResultSize", result == null ? 0 : result.length);
        return info;
    }
}
