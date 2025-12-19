package Database;

import java.io.Closeable;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import Database.InfoItem.InfoItem;
import Database.InfoItem.InfoAni.InfoAniFetch;

public class SQLiteAccess implements Closeable {

    private static final String sqlCreateAniTable = """
            CREATE TABLE "anime" (
                "ANI_ID"            integer NOT NULL,
                "air_date"          text,
                "title"             text,
                "title_cn"          text,
                "aliases"           text,
                "description"       text,
                "episode_count"     integer,
                "url_official_site" text,
                "url_cover"         text,
                "url_rss"           text,
                "rating_before"     integer,
                "rating_after"      integer,
                "remark"            text,
                PRIMARY KEY ("ANI_ID")
            );
            """;

    private static final String sqlCreateEpiTable = """
            CREATE TABLE "episode" (
                "EPI_ID"            integer NOT NULL,
                "ANI_ID"            integer NOT NULL,
                "ep"                text,
                "sort"              real,
                "air_date"          text,
                "duration"          integer,
                "title"             text,
                "title_cn"          text,
                "description"       text,
                "rating"            integer,
                "view_datetime"     text,
                "status_download"   text,
                "status_view"       text,
                "remark"            text,
                PRIMARY KEY ("EPI_ID"),
                CONSTRAINT "ANI_ID"
                FOREIGN KEY ("ANI_ID")
                REFERENCES "anime" ("ANI_ID")
                ON DELETE CASCADE ON UPDATE CASCADE
            );
            """;

    private static final String sqlCreateTorTable = """
            CREATE TABLE "torrent" (
                "TOR_URL"           text NOT NULL,
                "ANI_ID"            integer NOT NULL,
                "air_datetime"      text,
                "size"              integer,
                "url_page"          text,
                "title"             text,
                "subtitle_group"    text,
                "description"       text,
                "status_download"   text,
                "remark"            text,
                PRIMARY KEY ("TOR_URL"),
                CONSTRAINT "ANI_ID"
                FOREIGN KEY ("ANI_ID")
                REFERENCES "anime" ("ANI_ID")
                ON DELETE CASCADE ON UPDATE CASCADE
            );
            """;

    private Connection conn;

    private static void initDatabase(String db_url) throws SQLException {
        // 连接 SQLite 数据库
        System.out.println("Creating new database...");
        try (var conn = DriverManager.getConnection(db_url)) {
            conn.prepareStatement(sqlCreateAniTable).execute();
            conn.prepareStatement(sqlCreateEpiTable).execute();
            conn.prepareStatement(sqlCreateTorTable).execute();
        }
        System.out.println("Database created.");
    }

    public SQLiteAccess(String dbPath) throws SQLException {

        var dbUrl = "jdbc:sqlite:" + dbPath;

        // 检查数据库是否存在
        if (!new File(dbPath).exists()) {
            System.out.println("Database file not found.");
            initDatabase(dbUrl); // 如果表不存在则创建表
        }
        conn = DriverManager.getConnection(dbUrl); // 连接 SQLite 数据库
    }

    public void Upsert(List<InfoItem> items) {
        if (items == null)
            return;
        for (InfoItem it : items) {
            if (it instanceof InfoAniFetch) {
                try {
                    Upsert((InfoAniFetch) it);
                } catch (SQLException e) {
                    System.err
                            .println("Upsert failed for ANI_ID=" + ((InfoAniFetch) it).ANI_ID + ": " + e.getMessage());
                }
            }
            // 未来可扩展：处理其他 InfoItem 子类
        }
    }

    // 插入或更新单个 InfoAniFetch 项目
    public void Upsert(InfoAniFetch item) throws SQLException {
        final String sqlAniFetch = """
                INSERT INTO anime (
                    ANI_ID,
                    air_date,
                    title,
                    title_cn,
                    aliases,
                    description,
                    episode_count,
                    url_official_site,
                    url_cover
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (ANI_ID) DO UPDATE SET
                    air_date            = excluded.air_date,
                    title               = excluded.title,
                    title_cn            = excluded.title_cn,
                    aliases             = excluded.aliases,
                    description         = excluded.description,
                    episode_count       = excluded.episode_count,
                    url_official_site   = excluded.url_official_site,
                    url_cover           = excluded.url_cover;
                """;
        try (PreparedStatement ps = conn.prepareStatement(sqlAniFetch)) {
            ps.setInt(1, item.ANI_ID);
            ps.setString(2, item.getAirDateString());
            ps.setString(3, item.title);
            ps.setString(4, item.title_cn);
            ps.setString(5, item.aliases);
            ps.setString(6, item.description);
            ps.setInt(7, item.episode_count);
            ps.setString(8, item.url_official_site);
            ps.setString(9, item.url_cover);
            ps.executeUpdate();
        }
    }

    @Override
    public void close() {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.err.println("Close failed: " + e.getMessage());
            }
            conn = null;
        }
    }
}
