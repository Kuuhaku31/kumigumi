package Util;

import java.util.ArrayList;
import java.util.List;

import InfoItem.InfoAni.InfoAniStore;


public class StoreItemBuilderAni implements DatabaseItemBuilder {

    /**
     * TableData -> List<InfoAniStore>
     */
    @Override
    public List<InfoAniStore> build(TableData tableData) {

        // 获取列索引
        var aniIdIndex        = tableData.GetHeaderIndex("ANI_ID");
        var urlRSSIndex       = tableData.GetHeaderIndex("url_rss");
        var ratingBeforeIndex = tableData.GetHeaderIndex("rating_before");
        var ratingAfterIndex  = tableData.GetHeaderIndex("rating_after");
        var remarkIndex       = tableData.GetHeaderIndex("remark");

        // 遍历数据行，构造InfoAniStore对象并添加到列表
        List<InfoAniStore> infoList = new ArrayList<>();
        for(var row : tableData.GetData()) {

            // 解析 ANI_ID
            var info = new InfoAniStore(Integer.parseInt(row[aniIdIndex]));

            // 解析 url_rss
            if(urlRSSIndex != -1) info.url_rss = row[urlRSSIndex];

            // 解析 rating_before
            if(ratingBeforeIndex != -1) try {
                info.rating_before = Integer.parseInt(row[ratingBeforeIndex]);
            } catch(NumberFormatException _) {
                info.rating_before = null;
            }

            // 解析 rating_after
            if(ratingAfterIndex != -1) try {
                info.rating_after = Integer.parseInt(row[ratingAfterIndex]);
            } catch(NumberFormatException _) {
                info.rating_after = null;
            }

            // 解析 remark
            if(remarkIndex != -1) info.remark = row[remarkIndex];

            // 添加到列表
            infoList.add(info);
        }
        return infoList;
    }
}
