package Util;

import java.util.ArrayList;
import java.util.List;

import InfoItem.InfoAniTor.InfoAniTorStore;


public class StoreItemBuilderAniTor implements DatabaseItemBuilder {

    /**
     * TableData -> List<InfoAniTorStore>
     */
    @Override
    public List<InfoAniTorStore> build(TableData tableData) {

        // 获取列索引
        var aniIdIndex          = tableData.GetHeaderIndex("ANI_ID");
        var torHashIndex        = tableData.GetHeaderIndex("TOR_HASH");
        var statusDownloadIndex = tableData.GetHeaderIndex("status_download");
        var remarkIndex         = tableData.GetHeaderIndex("remark");

        // 遍历数据行，构造InfoAniTorStore对象并添加到列表
        List<InfoAniTorStore> infoList = new ArrayList<>();
        for(var row : tableData.GetData()) {

            // 解析 ANI_ID 和 TOR_HASH
            var info = new InfoAniTorStore(Integer.parseInt(row[aniIdIndex]), row[torHashIndex]);

            // 解析 status_download 和 remark
            if(statusDownloadIndex != -1) info.status_download = row[statusDownloadIndex];
            if(remarkIndex         != -1) info.remark          = row[remarkIndex];

            // 添加到列表
            infoList.add(info);
        }
        return infoList;
    }
}
