package InfoItem.InfoEpi;

import Database.Item.UpsertItem;

public class InfoEpiUpsert extends InfoEpi implements UpsertItem {

    public final Integer ANI_ID;

    public InfoEpiUpsert(Integer EPI_ID, Integer ANI_ID) {
        super(EPI_ID);
        this.ANI_ID = ANI_ID;
    }

    @Override
    public String toString() {
        return "InfoEpiUpsert{" + "EPI_ID=" + EPI_ID + ", ANI_ID=" + ANI_ID + '}';
    }

}
