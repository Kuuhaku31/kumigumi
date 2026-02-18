package FetchTask;

import java.io.IOException;
import java.util.List;

import NetAccess.NetAccess;
import Database.InfoItem.UpdateItem;
import Database.InfoItem.UpsertItem;
import Main.ItemTranslation;

public class FetchTaskTor extends FetchTask {

    private final String url_rss;
    private final Integer ani_id;

    public FetchTaskTor(String url_rss, Integer ani_id) {
        this.url_rss = url_rss;
        this.ani_id = ani_id;
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
            System.err.println("Error fetching torrent info for TOR_URL=" + url_rss + ": " + e.getMessage());
        }
    }

    @Override
    public String toString() {
        return "FetchTaskTor{TOR_URL=" + url_rss + ", ANI_ID=" + ani_id + "}";
    }
}