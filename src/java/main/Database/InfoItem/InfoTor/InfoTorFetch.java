package Database.InfoItem.InfoTor;

import java.time.OffsetDateTime;

public class InfoTorFetch extends InfoTor {

    public OffsetDateTime air_datetime;
    public Long size;
    public String url_page;
    public String title;
    public String subtitle_group;
    public String description;

    public InfoTorFetch(String TOR_URL) {
        super(TOR_URL);
    }

    @Override
    public String toString() {
        return "InfoTorFetch{" +
                "TOR_URL='" + TOR_URL + '\'' +
                ", air_datetime=" + air_datetime +
                ", size=" + size +
                ", url_page='" + url_page + '\'' +
                ", title='" + title + '\'' +
                ", subtitle_group='" + subtitle_group + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
