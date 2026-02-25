package InfoItem.InfoEpi;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import Database.Item.UpsertItem;
import InfoItem.InfoItem;
import Util.TableData;


public class InfoEpi extends InfoItem implements UpsertItem {

    public final Integer EPI_ID;
    public final Integer ANI_ID;

    public InfoEpi(Integer EPI_ID, Integer ANI_ID) {
        this.EPI_ID = EPI_ID;
        this.ANI_ID = ANI_ID;
    }

    /**
     * Map -> InfoEpi
     */
    public InfoEpi(Map<String, String> data) {

        // 参数检查
        if(data == null || data.isEmpty()) {
            throw new IllegalArgumentException("InfoEpi构造函数: 传入的Map<String, String>为null或空");
        }

        // 保证 EPI_ID 和 ANI_ID 存在
        if(!data.containsKey("EPI_ID") || !data.containsKey("ANI_ID")) {
            throw new IllegalArgumentException("InfoEpi构造函数: Map<String, String>缺少必需的键 'EPI_ID' 或 'ANI_ID'");
        }

        this.EPI_ID = Integer.parseInt(data.get("EPI_ID"));
        this.ANI_ID = Integer.parseInt(data.get("ANI_ID"));
    }


    /**
     * TableData -> List<InfoEpi>
     */
    public static List<InfoEpi> convertInfoEpi(TableData tableData) {

        var epiIdIndex = tableData.GetHeaderIndex("EPI_ID");
        var aniIdIndex = tableData.GetHeaderIndex("ANI_ID");
        if(epiIdIndex == -1 || aniIdIndex == -1) return null;

        var rows     = tableData.GetData();
        var infoList = new ArrayList<InfoEpi>();
        for(var row : rows) {
            var info = new InfoEpi(Integer.parseInt(row[epiIdIndex]), Integer.parseInt(row[aniIdIndex]));
            infoList.add(info);
        }
        return infoList;
    }

    @Override
    public String toString() {
        return "InfoEpi{" + "EPI_ID=" + EPI_ID + ", ANI_ID=" + ANI_ID + '}';
    }
}
