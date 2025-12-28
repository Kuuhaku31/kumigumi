package FetchTask;

import java.io.IOException;
import java.util.List;

import NetAccess.NetAccess;
import Database.InfoItem.UpdateItem;
import Database.InfoItem.UpsertItem;
import Main.ItemTranslation;

public class FetchTaskTor extends FetchTask {

    final String tor_url;
    final Integer ani_id;

    public FetchTaskTor(List<UpsertItem> bufferUpsert, List<UpdateItem> bufferUpdate, String tor_url, Integer ani_id) {
        super(bufferUpsert, bufferUpdate);
        this.tor_url = tor_url;
        this.ani_id = ani_id;
    }

    @Override
    public void run() {
        try {
            var torInfoList = NetAccess.FetchTorrentInfo(tor_url);
            for (var tor : torInfoList) {
                tor.put("ANI_ID", ani_id.toString());
                bufferUpsert.add(ItemTranslation.transTorUpsert(tor));
                bufferUpdate.add(ItemTranslation.convertInfoTorFetch(tor));
            }
        } catch (IOException e) {
            System.err.println("Error fetching torrent info for TOR_URL=" + tor_url + ": " + e.getMessage());
        }
    }
}