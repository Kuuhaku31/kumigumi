package FetchTask;

import InfoItem.InfoAniTor.InfoAniTorFetch;
import NetAccess.NetAccess;


public class FetchTaskAniTor extends FetchTask {

    final String  url_rss;
    final Integer ani_id;

    /**
     * 构造函数
     */
    public FetchTaskAniTor(FetchTaskManager manager, String url_rss, Integer ani_id) {

        super(manager);

        this.url_rss          = url_rss;
        this.ani_id           = ani_id;
    }

    @Override
    public void run() {
        try {
            var torInfoList = NetAccess.FetchAnimeTorrentInfo(url_rss);
            for(var tor : torInfoList) {
                tor.put("ANI_ID", ani_id.toString());
                manager.bufferUpdate.add(new InfoAniTorFetch(tor));
            }

            log += "Fetched " + torInfoList.size() + " torrent(s) for ANI_ID=" + ani_id + "\n";
            status = TaskStatus.SUCCESS; // 标记任务成功
        }
        catch(Exception e) {
            // System.err.println("Error fetching torrent info for URL_RSS=" + url_rss + ": " + e.getMessage());
            log += "Error fetching torrent info for URL_RSS=" + url_rss + ": " + e.getMessage() + "\n";

            status = TaskStatus.FAIL; // 标记任务失败
        }
        finally { taskFinally(); }
    }

    @Override
    public String toString() {
        return "FetchTaskAniTor{URL_RSS=" + url_rss + ", ANI_ID=" + ani_id + ", status=" + status + ", log=" + log.replace("\n", "\\n") + "}";
    }
}
