package Main.FetchTask;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Set;

import Info.BaseInfo;
import Info.TorrentInfo;
import Database.TorrentDownloader;
import NetAccess.NetAccess;


public class FetchTorrentInfoTask extends FetchInfoTask {

    final TorrentDownloader downloader;
    private TorrentInfo result = null;

    public FetchTorrentInfoTask(TorrentDownloader downloader) {
        this.downloader = downloader;
    }

    public void execute() {
        start();

        var url_list = downloader.getUrlList();
        for(var url : url_list) {

            byte[] file = null;
            try { file = NetAccess.DownloadFile(url); }
            catch (URISyntaxException | IOException _) {}

            if(file != null && file.length > 0) {
                result = new TorrentInfo(file);
                if(result != null) break;
            }
        }

        if(result == null) fail();
        else complete();
    }

    @Override
    public Map<String, Object> getInfo() {
        var info = super.getInfo();
        info.put("TOR_HASH", downloader);
        info.put("ResultSize", result == null ? 0 : result.file_size);
        return info;
    }

    @Override
    public Set<? extends BaseInfo> GetInfoSet() {
        if(result == null) return java.util.Set.of();
        return java.util.Set.of(result);
    }
}
