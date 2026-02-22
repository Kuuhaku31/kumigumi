package InfoItem.InfoTor;

import Database.Item.UpsertItem;

public class InfoTorUpsert extends InfoTor implements UpsertItem {

    public InfoTorUpsert(String TOR_HASH) {
        super(TOR_HASH);
    }
}
