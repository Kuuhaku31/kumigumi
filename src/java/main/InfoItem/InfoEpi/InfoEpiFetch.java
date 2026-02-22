package InfoItem.InfoEpi;

import static util.Util.getDateString;

import Database.Item.UpdateItem;
import java.util.Date;
import java.util.Map;


public class InfoEpiFetch extends InfoEpi implements UpdateItem {

    public Integer ep;
    public Float   sort;
    public Date    air_date;
    public Integer duration;
    public String  title;
    public String  title_cn;
    public String  description;

    public InfoEpiFetch(Integer EPI_ID, Integer ANI_ID) {
        super(EPI_ID, ANI_ID);
    }

    /**
     * Map -> InfoEpiFetch
     */
    public InfoEpiFetch(Map<String, String> data) {

        // 保证 EPI_ID 存在
        if(!data.containsKey("EPI_ID") || !data.containsKey("ANI_ID")) {
            throw new IllegalArgumentException("InfoEpiFetch构造函数: Map<String, String>缺少必需的键 'EPI_ID' 或 'ANI_ID'");
        }

        // 调用父类构造函数传递 EPI_ID 和 ANI_ID
        super(Integer.parseInt(data.get("EPI_ID")), Integer.parseInt(data.get("ANI_ID")));

        // 解析其他字段
        if(data.containsKey("ep")) this.ep = Integer.parseInt(data.get("ep"));
        if(data.containsKey("sort")) this.sort = Float.parseFloat(data.get("sort"));
        if(data.containsKey("air_date")) {
            var dateStr = data.get("air_date");
            if(dateStr != null) try {
                var  sdf        = new java.text.SimpleDateFormat("yyyy-MM-dd");
                Date parsedDate = sdf.parse(dateStr);
                this.air_date   = parsedDate;
            }
            catch(java.text.ParseException _) { this.air_date = null; }
        }
        if(data.containsKey("duration")) this.duration = data.get("duration") != null ? Integer.parseInt(data.get("duration")) : null;
        if(data.containsKey("title")) this.title = data.get("title");
        if(data.containsKey("title_cn")) this.title_cn = data.get("title_cn");
        if(data.containsKey("description")) this.description = data.get("description");
   }

    @Override
    public String toString() {
        return "InfoEpiFetch{"
            + "EPI_ID=" + EPI_ID 
            + ", ep=" + ep 
            + ", sort=" + sort 
            + ", air_date=" + getDateString(air_date) 
            + ", duration=" + duration 
            + ", title='" + title + '\''
            + ", title_cn='" + title_cn + '\''
            + ", description='" + description + '\''
            + '}';
    }
}
