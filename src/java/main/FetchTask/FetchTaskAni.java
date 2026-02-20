package FetchTask;

import Database.Item.UpdateItem;
import Database.Item.UpsertItem;
import Main.ItemTranslation;
import NetAccess.NetAccess;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;


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
        } catch(URISyntaxException | IOException e) {
            System.err.println("Error fetching anime info for ANI_ID=" + ani_id + ": " + e.getMessage());
        }
    }

    @Override
    public String toString() {
        return "FetchTaskAni{ANI_ID=" + ani_id + "}";
    }
}
