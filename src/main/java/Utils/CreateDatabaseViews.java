package Utils;

import java.sql.SQLException;

import Database.SQLiteAccess;


public final class CreateDatabaseViews {

    private CreateDatabaseViews() {}

    public static void main(String[] args) throws SQLException {
        if(args.length != 1 || args[0].isBlank()) {
            throw new IllegalArgumentException("Usage: Utils.CreateDatabaseViews <database-path>");
        }

        Create(args[0]);
        System.out.println("数据库视图创建完成: " + args[0]);
    }

    public static void Create(String databasePath) throws SQLException {
        if(databasePath == null || databasePath.isBlank()) {
            throw new IllegalArgumentException("Database path cannot be blank");
        }

        try(var db = new SQLiteAccess(databasePath)) {
            db.CreateViews();
        }
    }
}
