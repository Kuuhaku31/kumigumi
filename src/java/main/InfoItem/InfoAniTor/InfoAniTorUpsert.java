package InfoItem.InfoAniTor;

import Database.Item.UpsertItem;

public class InfoAniTorUpsert extends InfoAniTor implements UpsertItem {

    public InfoAniTorUpsert(Integer ANI_ID, String TOR_HASH) {
        super(ANI_ID, TOR_HASH);
    }

    @Override
    public String toString() {
        return "InfoAniTorUpsert{"
            + "TOR_HASH=" + TOR_HASH + ", ANI_ID=" + ANI_ID + '}';
    }
}
