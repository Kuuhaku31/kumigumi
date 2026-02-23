package FetchTask;

import java.io.IOException;

import InfoItem.InfoAniTor.InfoAniTor;
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
                manager.bufferUpsert.add(new InfoAniTor(tor));
                manager.bufferUpdate.add(new InfoAniTorFetch(tor));
                // manager.checkTorHashList.add(tor.get("TOR_HASH"));
            }
        } catch(IOException e) {
            System.err.println("Error fetching torrent info for URL_RSS=" + url_rss + ": " + e.getMessage());
        }
    }

    @Override
    public String toString() {
        return "FetchTaskTor{URL_RSS=" + url_rss + ", ANI_ID=" + ani_id + "}";
    }
}
