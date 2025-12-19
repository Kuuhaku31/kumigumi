package Database.InfoItem.InfoAni;

import Database.InfoItem.InfoItem;

public abstract class InfoAni extends InfoItem {
    final Integer ANI_ID;

    InfoAni(Integer ANI_ID) {
        this.ANI_ID = ANI_ID;
    }
}
