package Database.InfoItem.InfoTor;

public class InfoTorStore extends InfoTor {
    String status_download;
    String remark;

    InfoTorStore(Integer ANI_ID, String TOR_URL) {
        super(ANI_ID, TOR_URL);
    }
}
