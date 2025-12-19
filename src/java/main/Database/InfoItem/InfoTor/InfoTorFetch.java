package Database.InfoItem.InfoTor;

import java.time.LocalDateTime;

public class InfoTorFetch extends InfoTor {

    LocalDateTime air_datetime;
    Integer size;
    String url_page;
    String title;
    String subtitle_group;
    String description;

    InfoTorFetch(Integer ANI_ID, String TOR_URL) {
        super(ANI_ID, TOR_URL);
    }
}
