package TestDatabase;

import static TestDatabase.ARGS.DB_PATH;

import Database.SQLiteAccess;


public class CreateDB {
    public static void main(String[] args) {
        System.out.println("Hello, TestDatabase.CreateDB!");
        System.out.println("数据库路径: " + DB_PATH);
        createPath(DB_PATH);

        try(SQLiteAccess db = new SQLiteAccess(DB_PATH)) {
            System.out.println("数据库表创建成功: " + db.toString());
        } catch(Exception e) {
            System.err.println("创建数据库失败: " + e.getMessage());
        }
    }

    private static void createPath(String path) {
        var file   = new java.io.File(path);
        var parent = file.getParentFile();
        if(parent != null && !parent.exists()) {
            if(parent.mkdirs()) {
                System.out.println("创建目录成功: " + parent.getAbsolutePath());
            } else {
                System.err.println("创建目录失败: " + parent.getAbsolutePath());
            }
        }
    }
}
