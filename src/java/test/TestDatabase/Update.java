package TestDatabase;

import Database.Item.UpdateItem;
import java.util.ArrayList;
import java.util.List;

public class Update {
    public static void main(String[] args) {
        System.out.println("Hello, TestDatabase.Update!");

        System.out.println("测试 update 操作...");
        System.out.println("尝试连接数据库");
        try(var db = new Database.SQLiteAccess(ARGS.DB_PATH)) {
            System.out.println("连接成功: " + db.toString());

            List<UpdateItem> items = new ArrayList<>();
            items.add(ARGS.INFO_ANI_FETCH_1);
            items.add(ARGS.INFO_TOR_FETCH_1);

            // 尝试更新测试数据
            db.Update(items);

            System.out.println("update 操作成功");

        } catch(Exception e) {
            System.err.println("数据库操作失败: " + e.getMessage());
        }
    }
}
