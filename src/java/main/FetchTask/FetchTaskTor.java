package FetchTask;

import InfoItem.InfoTor.InfoTorFetch;
import NetAccess.NetAccess;

public class FetchTaskTor extends FetchTask {

    // 任务参数
    final String TOR_HASH;
    final String url_download;

    /**
     * 构造函数：创建 FetchTaskTor 实例
     * @param manager
     * @param TOR_HASH
     * @param url_download
     */
    public FetchTaskTor(FetchTaskManager manager, String TOR_HASH, String url_download) {
        super(manager);                   // 调用外部类构造函数
        this.TOR_HASH     = TOR_HASH;     // 初始化 TOR_HASH
        this.url_download = url_download; // 初始化 url_download
    }

    @Override
    public void run() {
        try {
            var torInfoByte = NetAccess.DownloadFile(url_download);
            var newInfoTor = new InfoTorFetch(TOR_HASH, torInfoByte);
            manager.bufferUpsert.add(newInfoTor);
            manager.bufferUpdate.add(newInfoTor);

            status = TaskStatus.SUCCESS; // 标记任务成功
        }
        catch(Exception e) {
            // System.err.println("Error fetching torrent info for URL=" + url_download + ": " + e.getMessage());
            log += "Error fetching torrent info for URL=" + url_download + ": " + e.getMessage() + "\n";

            status = TaskStatus.FAIL; // 标记任务失败
        }
        finally { taskFinally(); }
    }

    @Override
    public String toString() {
        return "FetchTaskTor{TOR_HASH=" + TOR_HASH + ", URL_DOWNLOAD=" + url_download + ", status=" + status + ", log=" + log.replace("\n", "\\n") + "}";
    }
}
