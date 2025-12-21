package Database.InfoItem.InfoEpi;

import Database.InfoItem.InfoItem;

public class InfoEpi extends InfoItem {
    public final Integer EPI_ID;

    public InfoEpi(Integer EPI_ID) {
        this.EPI_ID = EPI_ID;
    }

    @Override
    public String toString() {
        return "InfoEpi{" + "EPI_ID=" + EPI_ID + '}';
    }
}
