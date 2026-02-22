package InfoItem.InfoAniTor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import Database.Item.UpsertItem;
import InfoItem.InfoItem;
import util.TableData;


public class InfoAniTor extends InfoItem implements UpsertItem {

    public final Integer ANI_ID;
    public final String  TOR_HASH;

    public InfoAniTor(Integer ANI_ID, String TOR_HASH) {
        this.ANI_ID   = ANI_ID;
        this.TOR_HASH = TOR_HASH;
    }

    /**
     * Map -> InfoAniTor
     */
    public InfoAniTor(Map<String, String> data) {

        // 参数检查
        if(data == null || data.isEmpty()) {
            throw new IllegalArgumentException("InfoAniTor构造函数: 传入的Map<String, String>为null或空");
        }

        // 保证 TOR_HASH 和 ANI_ID 存在
        if(!data.containsKey("TOR_HASH") || !data.containsKey("ANI_ID")) {
            throw new IllegalArgumentException("InfoAniTor构造函数: Map<String, String>缺少必需的键 'TOR_HASH' 或 'ANI_ID'");
        }

        this.ANI_ID   = Integer.parseInt(data.get("ANI_ID"));
        this.TOR_HASH = data.get("TOR_HASH");
    }

    /**
     * TableData -> List<InfoAniTor>
     */
    public static List<InfoAniTor> convertInfoAniTor(TableData tableData) {

        var aniIdIndex  = tableData.GetHeaderIndex("ANI_ID");
        var torHashIndex = tableData.GetHeaderIndex("TOR_HASH");
        if(torHashIndex == -1 || aniIdIndex == -1) return null;

        var rows     = tableData.GetData();
        var infoList = new ArrayList<InfoAniTor>();
        for(var row : rows) {
            var info = new InfoAniTor(Integer.parseInt(row[aniIdIndex]), row[torHashIndex]);
            infoList.add(info);
        }
        return infoList;
    }

    @Override
    public String toString() {
        return "InfoAniTor{" + "ANI_ID=" + ANI_ID + ", TOR_HASH='" + TOR_HASH + '\'' + '}';
    }
}
