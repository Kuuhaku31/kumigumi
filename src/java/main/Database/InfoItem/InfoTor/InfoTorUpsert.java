package Database.InfoItem.InfoTor;

import Database.InfoItem.UpsertItem;

public class InfoTorUpsert extends InfoTor implements UpsertItem {

    public final Integer ANI_ID;

    public InfoTorUpsert(String TOR_URL, Integer ANI_ID) {
        super(TOR_URL);
        this.ANI_ID = ANI_ID;
    }

    @Override
    public String toString() {
        return "InfoTorUpsert{" + "TOR_URL=" + TOR_URL + ", ANI_ID=" + ANI_ID + '}';
    }

}
