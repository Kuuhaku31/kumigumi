package InfoItem.InfoAni;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import Database.Item.UpsertItem;
import InfoItem.InfoItem;
import util.TableData;


public class InfoAni extends InfoItem implements UpsertItem {
    public final Integer ANI_ID;

    public InfoAni(Integer ANI_ID) {
        this.ANI_ID = ANI_ID;
    }

    /**
     * Map -> InfoAni
     */
    public InfoAni(Map<String, String> data) {

        // 参数检查
        if(data == null || data.isEmpty()) {
            throw new IllegalArgumentException("InfoAni构造函数: 传入的Map<String, String>为null或空");
        }

        // 保证 ANI_ID 存在
        if(!data.containsKey("ANI_ID")) {
            throw new IllegalArgumentException("InfoAni构造函数: Map<String, String>缺少必需的键 'ANI_ID'");
        }

        this.ANI_ID = Integer.parseInt(data.get("ANI_ID"));
    }

     /**
     * TableData -> List<InfoAni>
     */
    public static List<InfoAni> convertInfoAni(TableData tableData) {

        var aniIdIndex = tableData.GetHeaderIndex("ANI_ID");
        if(aniIdIndex == -1) return null;

        var rows     = tableData.GetData();
        var infoList = new ArrayList<InfoAni>();
        for(var row : rows) {
            var info = new InfoAni(Integer.parseInt(row[aniIdIndex]));
            infoList.add(info);
        }
        return infoList;
    }

    @Override
    public String toString() {
        return "InfoAni{" + "ANI_ID=" + ANI_ID + '}';
    }
}
