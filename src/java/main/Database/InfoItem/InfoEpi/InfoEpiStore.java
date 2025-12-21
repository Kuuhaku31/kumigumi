package Database.InfoItem.InfoEpi;

import java.time.OffsetDateTime;

import Database.InfoItem.UpdateItem;

public class InfoEpiStore extends InfoEpi implements UpdateItem {

    public Integer rating;
    public OffsetDateTime view_datetime;
    public String status_download;
    public String status_view;
    public String remark;

    public InfoEpiStore(Integer EPI_ID) {
        super(EPI_ID);
    }

    @Override
    public String toString() {
        return "InfoEpiStore{" +
                "EPI_ID=" + EPI_ID +
                ", rating=" + rating +
                ", view_datetime=" + view_datetime +
                ", status_download='" + status_download + '\'' +
                ", status_view='" + status_view + '\'' +
                ", remark='" + remark + '\'' +
                '}';
    }
}
