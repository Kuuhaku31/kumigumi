package Database.InfoItem.InfoTor;

public class InfoTorUpsert extends InfoTor {

    public final Integer ANI_ID;

    public InfoTorUpsert(String TOR_URL, Integer ANI_ID) {
        super(TOR_URL);
        this.ANI_ID = ANI_ID;
    }

    @Override
    public String toString() {
        return "InfoTorUpsert{" + "TOR_URL=" + TOR_URL + ", ANI_ID=" + ANI_ID + '}';
    }

}
