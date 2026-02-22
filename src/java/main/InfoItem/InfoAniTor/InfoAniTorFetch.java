package InfoItem.InfoAniTor;

import Database.Item.UpdateItem;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Map;


public class InfoAniTorFetch extends InfoAniTor implements UpdateItem {

    public OffsetDateTime air_datetime;   // 发布时间
    public String         url_download;   // 下载 URL
    public String         url_page;       // 详情页 URL
    public String         title;          // 标题
    public String         subtitle_group; // 字幕组
    public String         description;    // 描述

    public InfoAniTorFetch(Integer ANI_ID, String TOR_HASH) { super(ANI_ID, TOR_HASH); }

    /**
     * Map -> InfoAniTorFetch
     */
    public InfoAniTorFetch(Map<String, String> data) {

        // 保证 TOR_URL 和 ANI_ID 存在
        if(!data.containsKey("TOR_URL") || !data.containsKey("ANI_ID")) {
            throw new IllegalArgumentException("InfoAniTorFetch构造函数: Map<String, String>缺少必需的键 'TOR_URL' 或 'ANI_ID'");
        }

        // 调用父类构造函数传递 ANI_ID 和 TOR_URL
        super(Integer.parseInt(data.get("ANI_ID")), data.get("TOR_URL"));

        // 解析其他字段
        if(data.containsKey("url_download")) this.url_download = data.get("url_download");
        if(data.containsKey("air_datetime")) {
            var datetimeStr = data.get("air_datetime");
            if(datetimeStr != null) try {
                var dateTime      = LocalDateTime.parse(datetimeStr);
                this.air_datetime = dateTime.atOffset(OffsetDateTime.now().getOffset());
            }
            catch(Exception _) { this.air_datetime = null; }
        }
        if(data.containsKey("url_page")) this.url_page = data.get("url_page");
        if(data.containsKey("title")) this.title = data.get("title");
        if(data.containsKey("subtitle_group")) this.subtitle_group = data.get("subtitle_group");
        if(data.containsKey("description")) this.description = data.get("description");
    }

    @Override
    public String toString() {
        return "InfoAniTorFetch{"
            + "ANI_ID=" + ANI_ID
            + ", TOR_HASH='" + TOR_HASH + '\''
            + ", air_datetime=" + air_datetime
            + ", url_download='" + url_download + '\''
            + ", url_page='" + url_page + '\'' + ", title='" + title + '\''
            + ", subtitle_group='" + subtitle_group + '\''
            + ", description='" + description + '\''
            + '}';
    }
}
