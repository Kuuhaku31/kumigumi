package TestDatabase;

import Database.Item.UpsertItem;
import InfoItem.InfoAni.InfoAniUpsert;
import InfoItem.InfoAniTor.InfoAniTorUpsert;
import InfoItem.InfoEpi.InfoEpiUpsert;
import InfoItem.InfoTor.InfoTorUpsert;
import java.util.ArrayList;
import java.util.List;

public class Upsert {
    public static void main(String[] args) {
        System.out.println("Hello, TestDatabase.Upsert!");

        System.out.println("测试 upsert 操作...");
        System.out.println("尝试连接数据库");
        try(var db = new Database.SQLiteAccess(ARGS.DB_PATH)) {
            System.out.println("连接成功");
            System.out.println(db.toString());


            List<UpsertItem> items = new ArrayList<>();
            items.add(new InfoAniUpsert(1));
            items.add(new InfoEpiUpsert(1, 2));
            items.add(new InfoTorUpsert(ARGS.TOR_HASH_1));
            items.add(new InfoAniTorUpsert(1, ARGS.TOR_HASH_1));

            db.Upsert(items);

            System.out.println("upsert 操作成功");

        } catch(Exception e) {
            System.err.println("数据库操作失败: " + e.getMessage());
        }
    }
}
