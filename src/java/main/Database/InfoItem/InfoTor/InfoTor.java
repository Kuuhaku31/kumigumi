package Database.InfoItem.InfoTor;

import Database.InfoItem.InfoItem;

public class InfoTor extends InfoItem {

    public final String TOR_URL;

    public InfoTor(String TOR_URL) {
        this.TOR_URL = TOR_URL;
    }

    @Override
    public String toString() {
        return "InfoTor{" + "TOR_URL='" + TOR_URL + '\'' + '}';
    }
}
