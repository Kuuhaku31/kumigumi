package NetAccess;

import MetaData.TestMetaData;

public class TestTorrentDownload {
    String tor_hash = "54491ff421a4df92ed295b9708a0888d13f2f63f";
    String tor_url  = "https://mikan.tangbai.cc/Download/20260220/54491ff421a4df92ed295b9708a0888d13f2f63f.torrent";

    public void main(String[] args) {
        var manager = new FetchTask.FetchTaskManager();
        manager.addFetchTaskTor(tor_hash, tor_url);
        manager.runAllTasks();
        manager.saveLog(TestMetaData.LOG_PATH, "TestTorrentDownload.log");
    }
}
