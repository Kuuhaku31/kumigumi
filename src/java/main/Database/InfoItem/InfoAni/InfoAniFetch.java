package Database.InfoItem.InfoAni;

import java.util.Date;
import static util.Util.getDateString;

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
                ", air_date=" + getDateString(air_date) +
                ", title='" + title + '\'' +
                ", title_cn='" + title_cn + '\'' +
                ", aliases='" + aliases + '\'' +
                ", description='" + description.replace("\r\n", "\\n") + '\'' +
                ", episode_count=" + episode_count +
                ", url_official_site='" + url_official_site + '\'' +
                ", url_cover='" + url_cover + '\'' +
                '}';
    }
}
