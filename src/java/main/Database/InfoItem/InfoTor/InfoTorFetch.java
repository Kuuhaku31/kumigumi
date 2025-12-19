package Database.InfoItem.InfoTor;

import java.time.LocalDateTime;
import static util.Util.getDateString;

public class InfoTorFetch extends InfoTor {

    public LocalDateTime air_datetime;
    public Integer size;
    public String url_page;
    public String title;
    public String subtitle_group;
    public String description;

    public InfoTorFetch(Integer ANI_ID, String TOR_URL) {
        super(ANI_ID, TOR_URL);
    }

    @Override
    public String toString() {
        return "InfoTorFetch{" +
                "ANI_ID=" + ANI_ID +
                ", TOR_URL='" + TOR_URL + '\'' +
                ", air_datetime=" + getDateString(air_datetime) +
                ", size=" + size +
                ", url_page='" + url_page + '\'' +
                ", title='" + title + '\'' +
                ", subtitle_group='" + subtitle_group + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
