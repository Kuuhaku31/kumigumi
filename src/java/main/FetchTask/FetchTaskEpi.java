package FetchTask;

import Database.Item.UpdateItem;
import Database.Item.UpsertItem;
import Main.ItemTranslation;
import NetAccess.NetAccess;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;


public class FetchTaskEpi extends FetchTask {

    final Integer ani_id;

    public FetchTaskEpi(List<UpsertItem> bufferUpsert, List<UpdateItem> bufferUpdate, Integer ani_id) {
        super(bufferUpsert, bufferUpdate);
        this.ani_id = ani_id;
    }

    @Override
    public void run() {
        try {
            var epiInfoList = NetAccess.FetchEpisodeInfo(ani_id);
            for(var epi : epiInfoList) {
                bufferUpsert.add(ItemTranslation.transEpiUpsert(epi));
                bufferUpdate.add(ItemTranslation.convertInfoEpiFetch(epi));
            }
        } catch(URISyntaxException | IOException e) {
            System.err.println("Error fetching episode info for ANI_ID=" + ani_id + ": " + e.getMessage());
        }
    }

    @Override
    public String toString() {
        return "FetchTaskEpi{ANI_ID=" + ani_id + "}";
    }
}
