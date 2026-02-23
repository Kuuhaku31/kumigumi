package TestDatabase;

import Database.Item.UpsertItem;
import InfoItem.InfoAni.InfoAni;
import InfoItem.InfoAniTor.InfoAniTor;
import InfoItem.InfoEpi.InfoEpi;
import InfoItem.InfoTor.InfoTor;
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
            items.add(new InfoAni(1));
            items.add(new InfoEpi(1, 2));
            items.add(new InfoTor(ARGS.TOR_HASH_1));
            items.add(new InfoAniTor(1, ARGS.TOR_HASH_1));

            db.Upsert(items);

            System.out.println("upsert 操作成功");

        } catch(Exception e) {
            System.err.println("数据库操作失败: " + e.getMessage());
        }
    }
}
