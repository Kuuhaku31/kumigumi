package Database.InfoItem.InfoEpi;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class InfoEpiStore extends InfoEpi {

    public Integer rating;
    public LocalDateTime view_datetime;
    public String status_download;
    public String status_view;
    public String remark;

    public InfoEpiStore(Integer ANI_ID, Integer EPI_ID) {
        super(ANI_ID, EPI_ID);
    }

    @Override
    public String toString() {
        var fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");
        var viewStr = (view_datetime == null) ? null : view_datetime.atZone(ZoneId.systemDefault()).format(fmt);

        return "InfoEpiStore{" +
                "ANI_ID=" + ANI_ID +
                ", EPI_ID=" + EPI_ID +
                ", rating=" + rating +
                ", view_datetime=" + viewStr +
                ", status_download='" + status_download + '\'' +
                ", status_view='" + status_view + '\'' +
                ", remark='" + remark + '\'' +
                '}';
    }
}
