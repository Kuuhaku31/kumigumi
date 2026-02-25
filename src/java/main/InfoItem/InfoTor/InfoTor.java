package InfoItem.InfoTor;

import Database.Item.UpsertItem;
import InfoItem.InfoItem;
import java.util.Map;


public class InfoTor extends InfoItem implements UpsertItem {
    public final String TOR_HASH;

    public InfoTor(String TOR_HASH) {
        this.TOR_HASH = TOR_HASH;
    }

    public InfoTor(Map<String, String> data) {

        // 参数检查
        if(data == null || data.isEmpty()) {
            throw new IllegalArgumentException("InfoTor构造函数: 传入的Map<String, String>为null或空");
        }

        // 保证 TOR_HASH 存在
        if(!data.containsKey("TOR_HASH")) {
            throw new IllegalArgumentException("InfoTor构造函数: Map<String, String>缺少必需的键 'TOR_HASH'");
        }

        this.TOR_HASH = data.get("TOR_HASH");
    }
    
    @Override
    public String toString() {
        return "InfoTor{" + "TOR_HASH='" + TOR_HASH + '\'' + '}';
    }
}
