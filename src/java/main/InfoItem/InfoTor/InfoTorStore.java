package InfoItem.InfoTor;

import Database.Item.UpdateItem;

public class InfoTorStore extends InfoTor implements UpdateItem {

    public final String remark; // 备注

    

    public InfoTorStore(String TOR_HASH, String remark) {
        super(TOR_HASH);
        this.remark = remark;
    }
}
