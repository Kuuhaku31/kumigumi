package Database.InfoItem.InfoAni;

import Database.InfoItem.InfoItem;

public class InfoAni extends InfoItem {
    public final Integer ANI_ID;

    public InfoAni(Integer ANI_ID) {
        this.ANI_ID = ANI_ID;
    }

    @Override
    public String toString() {
        return "InfoAni{" + "ANI_ID=" + ANI_ID + '}';
    }
}
