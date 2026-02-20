package InfoItem.InfoAniTor;

import Database.Item.UpdateItem;
import java.time.OffsetDateTime;


public class InfoAniTorFetch extends InfoAniTor implements UpdateItem {

    public OffsetDateTime air_datetime;   // 发布时间
    public String         url_download;   // 下载 URL
    public String         url_page;       // 详情页 URL
    public String         title;          // 标题
    public String         subtitle_group; // 字幕组
    public String         description;    // 描述

    public InfoAniTorFetch(Integer ANI_ID, String TOR_HASH) { super(ANI_ID, TOR_HASH); }

    @Override
    public String toString() {
        return "InfoAniTorFetch{"
            + "TOR_HASH='" + TOR_HASH + '\''
            + ", air_datetime=" + air_datetime + ", url_download='" + url_download + '\''
            + ", url_page='" + url_page + '\'' + ", title='" + title + '\''
            + ", subtitle_group='" + subtitle_group + '\''
            + ", description='" + description + '\''
            + '}';
    }
}
