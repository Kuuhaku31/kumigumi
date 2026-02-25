package InfoItem.InfoAniTor;

import Database.Item.UpdateItem;


public class InfoAniTorStore extends InfoAniTor implements UpdateItem {

    public String status_download;
    public String remark;

    public InfoAniTorStore(Integer ANI_ID, String TOR_HASH) {
        super(ANI_ID, TOR_HASH);
    }

    @Override
    public String toString() {
        return "InfoAniTorStore{"
            + "ANI_ID=" + ANI_ID
            + ", TOR_HASH='" + TOR_HASH + '\''
            + ", status_download='" + status_download + '\''
            + ", remark='" + remark + '\''
            + '}';
    }
}
