package Database.InfoItem.InfoEpi;

import Database.InfoItem.InfoItem;

public abstract class InfoEpi extends InfoItem {
    final Integer ANI_ID;
    final Integer EPI_ID;

    InfoEpi(Integer ANI_ID, Integer EPI_ID) {
        this.ANI_ID = ANI_ID;
        this.EPI_ID = EPI_ID;
    }
}
