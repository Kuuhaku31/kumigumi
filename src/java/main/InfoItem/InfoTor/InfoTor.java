package InfoItem.InfoTor;

import InfoItem.InfoItem;

public class InfoTor extends InfoItem {
    public final String TOR_HASH;

    public InfoTor(String TOR_HASH) {
        this.TOR_HASH = TOR_HASH;
    }

    @Override
    public String toString() {
        return "InfoTor{"
            + "TOR_HASH='" + TOR_HASH + '\'' + '}';
    }
}
