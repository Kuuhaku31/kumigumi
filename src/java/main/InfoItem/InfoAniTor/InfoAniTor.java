package InfoItem.InfoAniTor;

import InfoItem.InfoItem;

public class InfoAniTor extends InfoItem {

    public final Integer ANI_ID;
    public final String  TOR_HASH;

    public InfoAniTor(Integer ANI_ID, String TOR_HASH) {
        this.ANI_ID   = ANI_ID;
        this.TOR_HASH = TOR_HASH;
    }

    @Override
    public String toString() {
        return "InfoAniTor{"
            + "ANI_ID=" + ANI_ID + ", TOR_HASH='" + TOR_HASH + '\'' + '}';
    }
}
