package Database;

import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLiteInit {

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

    static void initDatabase(String db_url) throws SQLException {
        // 连接 SQLite 数据库
        System.out.println("Creating new database...");
        try (var conn = DriverManager.getConnection(db_url)) {
            conn.prepareStatement(sqlCreateAniTable).execute();
            conn.prepareStatement(sqlCreateEpiTable).execute();
            conn.prepareStatement(sqlCreateTorTable).execute();
        }
        System.out.println("Database created.");
    }
}
