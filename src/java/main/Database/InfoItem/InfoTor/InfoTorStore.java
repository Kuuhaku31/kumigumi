package Database.InfoItem.InfoTor;

public class InfoTorStore extends InfoTor {
    public String status_download;
    public String remark;

    public InfoTorStore(String TOR_URL) {
        super(TOR_URL);
    }

    @Override
    public String toString() {
        return "InfoTorStore{" +
                "TOR_URL='" + TOR_URL + '\'' +
                ", status_download='" + status_download + '\'' +
                ", remark='" + remark + '\'' +
                '}';
    }
}
