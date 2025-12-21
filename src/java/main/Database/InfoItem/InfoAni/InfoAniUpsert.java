package Database.InfoItem.InfoAni;

import Database.InfoItem.UpsertItem;

public class InfoAniUpsert extends InfoAni implements UpsertItem {

    public InfoAniUpsert(Integer ANI_ID) {
        super(ANI_ID);
    }

    @Override
    public String toString() {
        return "InfoAniUpsert{" + "ANI_ID=" + ANI_ID + '}';
    }

}
