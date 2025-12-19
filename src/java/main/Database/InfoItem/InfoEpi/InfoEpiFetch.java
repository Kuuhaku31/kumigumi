package Database.InfoItem.InfoEpi;

import java.util.Date;

public class InfoEpiFetch extends InfoEpi {

    public Integer ep;
    public Float sort;
    public Date air_date;
    public Integer duration;
    public String title;
    public String title_cn;
    public String description;

    public InfoEpiFetch(Integer ANI_ID, Integer EPI_ID) {
        super(ANI_ID, EPI_ID);
    }

    @Override
    public String toString() {
        return "InfoEpiFetch{" +
                "ANI_ID=" + ANI_ID +
                ", EPI_ID=" + EPI_ID +
                ", ep=" + ep +
                ", sort=" + sort +
                // 以 yyyy-MM-dd 格式显示 air_date
                ", air_date=" + (air_date != null ? String.format("%tF", air_date) : "null") +
                ", duration=" + duration +
                ", title='" + title + '\'' +
                ", title_cn='" + title_cn + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
