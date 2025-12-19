package Database.InfoItem.InfoTor;

import Database.InfoItem.InfoItem;

public abstract class InfoTor extends InfoItem {

    final String TOR_URL;
    final Integer ANI_ID;

    InfoTor(Integer ANI_ID, String TOR_URL) {
        this.ANI_ID = ANI_ID;
        this.TOR_URL = TOR_URL;
    }
}
