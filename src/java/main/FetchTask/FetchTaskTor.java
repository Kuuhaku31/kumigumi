package FetchTask;

import java.io.IOException;
import java.util.List;

import NetAccess.NetAccess;
import Database.InfoItem.UpdateItem;
import Database.InfoItem.UpsertItem;
import Main.ItemTranslation;

class FetchTaskTor extends FetchTask {

    final String url_rss;
    final Integer ani_id;

    final List<String> checkTorHashList;

    /** 构造函数 */
    FetchTaskTor(
            List<UpsertItem> bufferUpsert,
            List<UpdateItem> bufferUpdate,
            List<String> checkTorHashList,
            String url_rss, Integer ani_id) {
        super(bufferUpsert, bufferUpdate);
        this.url_rss = url_rss;
        this.ani_id = ani_id;
        this.checkTorHashList = checkTorHashList;
    }

    @Override
    public void run() {
        try {
            var torInfoList = NetAccess.FetchTorrentInfo(url_rss);
            for (var tor : torInfoList) {
                tor.put("ANI_ID", ani_id.toString());
                bufferUpsert.add(ItemTranslation.transTorUpsert(tor));
                bufferUpdate.add(ItemTranslation.convertInfoTorFetch(tor));
            }
        } catch (IOException e) {
            System.err.println("Error fetching torrent info for URL_RSS=" + url_rss + ": " + e.getMessage());
        }
    }

    @Override
    public String toString() {
        return "FetchTaskTor{URL_RSS=" + url_rss + ", ANI_ID=" + ani_id + "}";
    }
}