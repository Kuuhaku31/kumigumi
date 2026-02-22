package InfoItem.InfoAni;

import Database.Item.UpdateItem;
import java.util.ArrayList;
import java.util.List;
import util.TableData;


public class InfoAniStore extends InfoAni implements UpdateItem {

    public String  url_rss;
    public Integer rating_before;
    public Integer rating_after;
    public String  remark;

    public InfoAniStore(Integer ANI_ID) {
        super(ANI_ID);
    }

    /**
     * TableData -> List<InfoAniStore>
     */
    public static List<InfoAniStore> convertInfoAniStore(TableData tableData) {

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
    
    @Override
    public String toString() {
        return "InfoAniStore{"
            + "ANI_ID=" + ANI_ID
            + ", url_rss='" + url_rss + '\''
            + ", rating_before=" + rating_before
            + ", rating_after=" + rating_after
            + ", remark='" + remark + '\''
            + '}';
    }
}
