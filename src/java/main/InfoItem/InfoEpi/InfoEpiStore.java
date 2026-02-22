package InfoItem.InfoEpi;

import Database.Item.UpdateItem;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import util.TableData;


public class InfoEpiStore extends InfoEpi implements UpdateItem {

    public Integer        rating;
    public OffsetDateTime view_datetime;
    public String         status_download;
    public String         status_view;
    public String         remark;

    public InfoEpiStore(Integer EPI_ID, Integer ANI_ID) {
        super(EPI_ID, ANI_ID);
    }

    /**
     * TableData -> List<InfoEpiStore>
     */
    public static List<InfoEpiStore> convertInfoEpiStore(TableData tableData) {

        // 获取列索引
        var epiIdIndex          = tableData.GetHeaderIndex("EPI_ID");
        var aniIdIndex          = tableData.GetHeaderIndex("ANI_ID");
        var ratingIndex         = tableData.GetHeaderIndex("rating");
        var viewDatetimeIndex   = tableData.GetHeaderIndex("view_datetime");
        var statusDownloadIndex = tableData.GetHeaderIndex("status_download");
        var statusViewIndex     = tableData.GetHeaderIndex("status_view");
        var remarkIndex         = tableData.GetHeaderIndex("remark");

        // 遍历数据行，构造InfoEpiStore对象并添加到列表
        List<InfoEpiStore> infoList = new ArrayList<>();
        for(var row : tableData.GetData()) {

            // 确保 EPI_ID 和 ANI_ID 存在且有效
            var epi_id = row[epiIdIndex] == null ? null : Integer.parseInt(row[epiIdIndex]);
            var ani_id = row[aniIdIndex] == null ? null : Integer.parseInt(row[aniIdIndex]);
            if(epi_id == null || ani_id == null) continue;

            // 构造 InfoEpiStore 对象
            var info = new InfoEpiStore(epi_id, ani_id);

            // 解析其他字段
            if(ratingIndex         != -1 && row[ratingIndex      ] != null) info.rating          = Integer.parseInt(row[ratingIndex]);
            if(viewDatetimeIndex   != -1 && row[viewDatetimeIndex] != null) info.view_datetime   = OffsetDateTime.parse(row[viewDatetimeIndex]);
            if(statusDownloadIndex != -1                                  ) info.status_download = row[statusDownloadIndex];
            if(statusViewIndex     != -1                                  ) info.status_view     = row[statusViewIndex];
            if(remarkIndex         != -1                                  ) info.remark          = row[remarkIndex];

            // 添加到列表
            infoList.add(info);
        }
        return infoList;
    }

    @Override
    public String toString() {
        return "InfoEpiStore{"
            + "EPI_ID=" + EPI_ID
            + ", ANI_ID=" + ANI_ID
            + ", rating=" + rating
            + ", view_datetime=" + view_datetime
            + ", status_download='" + status_download + '\''
            + ", status_view='" + status_view + '\''
            + ", remark='" + remark + '\''
            + '}';
    }
}
