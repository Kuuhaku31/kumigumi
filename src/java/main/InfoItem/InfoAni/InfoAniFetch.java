package InfoItem.InfoAni;

import Database.Item.UpdateItem;
import java.util.Date;
import java.util.Map;


public class InfoAniFetch extends InfoAni implements UpdateItem {

    public Date    air_date;
    public String  title;
    public String  title_cn;
    public String  aliases;
    public String  description;
    public Integer episode_count;
    public String  url_official_site;
    public String  url_cover;

    public InfoAniFetch(Integer ani_id) {
        super(ani_id);
    }

    /**
     * Map -> InfoAniFetch
     */
    public InfoAniFetch(Map<String, String> data) {

        // 保证 ANI_ID 存在
        if(!data.containsKey("ANI_ID")) {
            throw new IllegalArgumentException("InfoAniFetch构造函数: Map<String, String>缺少必需的键 'ANI_ID'");
        }

        // 调用父类构造函数传递 ANI_ID
        super(Integer.parseInt(data.get("ANI_ID")));

        // 解析其他字段
        if(data.containsKey("air_date")) {
            var dateStr = data.get("air_date");
            if(dateStr != null) try {
                var  sdf        = new java.text.SimpleDateFormat("yyyy-MM-dd");
                Date parsedDate = sdf.parse(dateStr);
                this.air_date   = parsedDate;
            }
            catch(java.text.ParseException _) { this.air_date = null; }
        }
        if(data.containsKey("title"            )) this.title             = data.get("title");
        if(data.containsKey("title_cn"         )) this.title_cn          = data.get("title_cn");
        if(data.containsKey("aliases"          )) this.aliases           = data.get("aliases");
        if(data.containsKey("description"      )) this.description       = data.get("description");
        if(data.containsKey("episode_count"    )) this.episode_count     = Integer.parseInt(data.get("episode_count"));
        if(data.containsKey("url_official_site")) this.url_official_site = data.get("url_official_site");
        if(data.containsKey("url_cover"        )) this.url_cover         = data.get("url_cover");
    }

    @Override
    public String toString() {
        return "InfoAniFetch{"
            + "ANI_ID=" + ANI_ID
            + ", air_date=" + Util.Util.getDateString(air_date)
            + ", title='" + title + '\''
            + ", title_cn='" + title_cn + '\''
            + ", aliases='" + aliases + '\''
            + ", description='" + (description == null ? "null" : description.replace("\r\n", "\\n")) + '\''
            + ", episode_count=" + episode_count
            + ", url_official_site='" + url_official_site + '\''
            + ", url_cover='" + url_cover + '\'' + '}';
    }
}
