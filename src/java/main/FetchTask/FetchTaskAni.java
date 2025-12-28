package FetchTask;

import Database.InfoItem.UpdateItem;
import Database.InfoItem.UpsertItem;
import Main.ItemTranslation;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import NetAccess.NetAccess;

public class FetchTaskAni extends FetchTask {

    final Integer ani_id;

    public FetchTaskAni(List<UpsertItem> bufferUpsert, List<UpdateItem> bufferUpdate, Integer ani_id) {
        super(bufferUpsert, bufferUpdate);
        this.ani_id = ani_id;
    }

    @Override
    public void run() {
        try {
            var aniInfo = NetAccess.FetchAnimeInfo(ani_id);
            bufferUpsert.add(ItemTranslation.transAniUpsert(aniInfo));
            bufferUpdate.add(ItemTranslation.convertInfoAniFetch(aniInfo));
        } catch (URISyntaxException | IOException e) {
            System.err.println("Error fetching anime info for ANI_ID=" + ani_id + ": " + e.getMessage());
        }
    }
}
