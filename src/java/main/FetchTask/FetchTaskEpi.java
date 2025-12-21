package FetchTask;

import Database.InfoItem.UpdateItem;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import static Main.TableToInfo.convertInfoEpiFetch;
import static NetAccess.NetAccess.FetchEpisodeInfo;;

public class FetchTaskEpi extends FetchTask {

    final Integer ani_id;

    public FetchTaskEpi(List<UpdateItem> buffer, Integer ani_id) {
        super(buffer);
        this.ani_id = ani_id;
    }

    @Override
    public void run() {
        try {
            var epiInfoList = FetchEpisodeInfo(ani_id);
            for (var epi : epiInfoList)
                buffer.add(convertInfoEpiFetch(epi));
        } catch (URISyntaxException | IOException e) {
            System.err.println("Error fetching episode info for ANI_ID=" + ani_id + ": " + e.getMessage());
        }
    }

}
