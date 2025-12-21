package Database.InfoItem.InfoAni;

import Database.InfoItem.UpdateItem;

public class InfoAniStore extends InfoAni implements UpdateItem {

    public String url_rss;
    public Integer rating_before;
    public Integer rating_after;
    public String remark;

    public InfoAniStore(Integer ANI_ID) {
        super(ANI_ID);
    }

    @Override
    public String toString() {
        return "InfoAniStore{" +
                "ANI_ID=" + ANI_ID +
                ", url_rss='" + url_rss + '\'' +
                ", rating_before=" + rating_before +
                ", rating_after=" + rating_after +
                ", remark='" + remark + '\'' +
                '}';
    }
}
