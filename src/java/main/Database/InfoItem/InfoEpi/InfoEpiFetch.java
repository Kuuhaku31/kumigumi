package Database.InfoItem.InfoEpi;

import java.util.Date;

import Database.InfoItem.UpdateItem;

import static util.Util.getDateString;

public class InfoEpiFetch extends InfoEpi implements UpdateItem {

    public Integer ep;
    public Float sort;
    public Date air_date;
    public Integer duration;
    public String title;
    public String title_cn;
    public String description;

    public InfoEpiFetch(Integer EPI_ID) {
        super(EPI_ID);
    }

    @Override
    public String toString() {
        return "InfoEpiFetch{" +
                "EPI_ID=" + EPI_ID +
                ", ep=" + ep +
                ", sort=" + sort +
                ", air_date=" + getDateString(air_date) +
                ", duration=" + duration +
                ", title='" + title + '\'' +
                ", title_cn='" + title_cn + '\'' +
                ", description='" +
                (description == null ? null
                        : description.replace("\r", "\\r").replace("\n", "\\n"))
                + '\'' +
                '}';
    }
}
