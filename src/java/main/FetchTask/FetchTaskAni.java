package FetchTask;

import Database.InfoItem.UpdateItem;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import static Main.TableToInfo.convertInfoAniFetch;
import static NetAccess.NetAccess.FetchAnimeInfo;

public class FetchTaskAni extends FetchTask {

    final Integer ani_id;

    public FetchTaskAni(List<UpdateItem> buffer, Integer ani_id) {
        super(buffer);
        this.ani_id = ani_id;
    }

    @Override
    public void run() {
        try {
            var aniInfo = FetchAnimeInfo(ani_id);
            buffer.add(convertInfoAniFetch(aniInfo));
        } catch (URISyntaxException | IOException e) {
            System.err.println("Error fetching anime info for ANI_ID=" + ani_id + ": " + e.getMessage());
        }
    }
}
