package db;

import java.sql.SQLException;

import Database.InfoItem.InfoAni.InfoAniFetch;

public class TestDB {
    public static void main(String[] args) throws SQLException {
        System.out.println("TestDB");

        var infoAniFetch = new InfoAniFetch(11);
        infoAniFetch.air_date = new java.util.Date();
        infoAniFetch.title = "Test Title 222";
        infoAniFetch.title_cn = "测试标题222";
        infoAniFetch.aliases = "Alias1; Alias2";
        infoAniFetch.description = "This is a test description.";
        infoAniFetch.episode_count = 14;
        infoAniFetch.url_official_site = "https://example.com";
        infoAniFetch.url_cover = "https://example.com/cover.jpg";
        System.out.println(infoAniFetch);

        var dbURL = "D:/repositories/kumigumi/resources/test.db";
        try (var db = new Database.SQLiteAccess(dbURL)) {
            System.out.println("Database opened.");
            db.Upsert(infoAniFetch);
        }
    }
}
