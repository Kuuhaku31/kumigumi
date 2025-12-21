package FetchTask;

import java.io.IOException;
import java.util.List;
import static Main.TableToInfo.convertInfoTorFetch;
import static NetAccess.NetAccess.FetchTorrentInfo;
import Database.InfoItem.UpdateItem;

public class FetchTaskTor extends FetchTask {

    final String tor_url;
    final Integer ani_id;

    public FetchTaskTor(List<UpdateItem> buffer, String tor_url, Integer ani_id) {
        super(buffer);
        this.tor_url = tor_url;
        this.ani_id = ani_id;
    }

    @Override
    public void run() {
        try {
            var torInfoList = FetchTorrentInfo(tor_url);
            for (var tor : torInfoList) {
                tor.put("ANI_ID", ani_id.toString());
                buffer.add(convertInfoTorFetch(tor));
            }
        } catch (IOException e) {
            System.err.println("Error fetching torrent info for TOR_URL=" + tor_url + ": " + e.getMessage());
        }
    }
}