package InfoItem.InfoAniTor;

import Database.Item.UpdateItem;
import java.util.ArrayList;
import java.util.List;
import util.TableData;


public class InfoAniTorStore extends InfoAniTor implements UpdateItem {

    public String status_download;
    public String remark;

    public InfoAniTorStore(Integer ANI_ID, String TOR_HASH) {
        super(ANI_ID, TOR_HASH);
    }

    /**
     * TableData -> List<InfoAniTorStore>
     */
    public static List<InfoAniTorStore> convertInfoAniTorStore(TableData tableData) {

        // 获取列索引
        var aniIdIndex          = tableData.GetHeaderIndex("ANI_ID");
        var torUrlIndex         = tableData.GetHeaderIndex("TOR_URL");
        var statusDownloadIndex = tableData.GetHeaderIndex("status_download");
        var remarkIndex         = tableData.GetHeaderIndex("remark");

        // 遍历数据行，构造InfoAniTorStore对象并添加到列表
        List<InfoAniTorStore> infoList = new ArrayList<>();
        for(var row : tableData.GetData()) {

            // 解析 ANI_ID 和 TOR_URL
            var info = new InfoAniTorStore(Integer.parseInt(row[aniIdIndex]), row[torUrlIndex]);

            // 解析 status_download 和 remark
            if(statusDownloadIndex != -1) info.status_download = row[statusDownloadIndex];
            if(remarkIndex         != -1) info.remark          = row[remarkIndex];

            // 添加到列表
            infoList.add(info);
        }
        return infoList;
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
