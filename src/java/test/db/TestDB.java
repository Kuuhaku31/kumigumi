package db;

import java.sql.SQLException;

import Def.TestInfo;

public class TestDB {
    public static void main(String[] args) throws SQLException {
        System.out.println("TestDB");

        var dbURL = "D:/repositories/kumigumi/resources/test.db";
        try (var db = new Database.SQLiteAccess(dbURL)) {
            System.out.println("Database opened.");
            db.Upsert(TestInfo.infoAniFetch);
            db.Upsert(TestInfo.infoAniStore);
            db.Upsert(TestInfo.infoEpiFetch);
            db.Upsert(TestInfo.infoEpiStore);
            db.Upsert(TestInfo.infoTorFetch);
            db.Upsert(TestInfo.infoTorStore);
        }
    }
}
