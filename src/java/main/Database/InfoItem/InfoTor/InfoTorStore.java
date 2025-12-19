package Database.InfoItem.InfoTor;

public class InfoTorStore extends InfoTor {
    public String status_download;
    public String remark;

    public InfoTorStore(Integer ANI_ID, String TOR_URL) {
        super(ANI_ID, TOR_URL);
    }

    @Override
    public String toString() {
        return "InfoTorStore{" +
                "ANI_ID=" + ANI_ID +
                ", TOR_URL='" + TOR_URL + '\'' +
                ", status_download='" + status_download + '\'' +
                ", remark='" + remark + '\'' +
                '}';
    }
}
