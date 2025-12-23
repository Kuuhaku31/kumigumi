package FetchTask;

import Database.InfoItem.UpdateItem;
import Database.InfoItem.UpsertItem;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import Main.ItemTranslation;
import NetAccess.NetAccess;
import util.Loger;;

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
            for (var epi : epiInfoList) {
                bufferUpsert.add(ItemTranslation.transieEpiUpsert(epi));
                bufferUpdate.add(ItemTranslation.convertInfoEpiFetch(epi));

                // 打印所有键值对，用于调试
                var epi_str = "Map:{ ";
                for (var entry : epi.entrySet())
                    epi_str = epi_str + entry.getKey() + "=" + entry.getValue() + "; ";
                epi_str = epi_str + "}";
                epi_str = epi_str.replace("\r", "\\r").replace("\n", "\\n");
                Loger.log = Loger.log + "\nFetched episode info for ANI_ID=" + ani_id + ": " + epi_str;

            }
        } catch (URISyntaxException | IOException e) {
            System.err.println("Error fetching episode info for ANI_ID=" + ani_id + ": " + e.getMessage());
        }
    }

}
