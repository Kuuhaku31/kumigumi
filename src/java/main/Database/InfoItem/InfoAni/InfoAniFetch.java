package Database.InfoItem.InfoAni;

import java.util.Date;

public class InfoAniFetch extends InfoAni {

    public Date air_date;
    public String title;
    public String title_cn;
    public String aliases;
    public String description;
    public Integer episode_count;
    public String url_official_site;
    public String url_cover;

    public InfoAniFetch(Integer ani_id) {
        super(ani_id);
    }

    @Override
    public String toString() {
        return "InfoAniFetch{" +
                "ANI_ID=" + ANI_ID +
                // 以 yyyy-MM-dd 格式显示 air_date
                ", air_date=" + (air_date != null ? String.format("%tF", air_date) : "null") +
                ", title='" + title + '\'' +
                ", title_cn='" + title_cn + '\'' +
                ", aliases='" + aliases + '\'' +
                ", description='" + description + '\'' +
                ", episode_count=" + episode_count +
                ", url_official_site='" + url_official_site + '\'' +
                ", url_cover='" + url_cover + '\'' +
                '}';
    }
}
