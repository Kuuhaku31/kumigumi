package Database;

import java.sql.DriverManager;
import java.sql.SQLException;

class SQLiteInit {

    private static final String sqlCreateAnimeTable =
        """
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

        PRIMARY KEY ("ANI_ID" DESC)
        );
        """;

    private static final String sqlCreateEpisodeTable =
        """
        CREATE TABLE "episode" (

        "EPI_ID"          integer NOT NULL,
        "ANI_ID"          integer NOT NULL,

        "ep"              text,
        "sort"            real,
        "air_date"        text,
        "duration"        integer,
        "title"           text,
        "title_cn"        text,
        "description"     text,

        "rating"          integer,
        "view_datetime"   text,
        "status_download" text,
        "status_view"     text,
        "remark"          text,

        PRIMARY KEY ("EPI_ID" DESC),

        CONSTRAINT "ANI_ID"
        FOREIGN KEY ("ANI_ID") 
        REFERENCES "anime" ("ANI_ID") 
        ON DELETE CASCADE ON UPDATE CASCADE
        );
        """;

    private static final String sqlCreateTorrentTable =
        """
        CREATE TABLE "torrent" (

        "TOR_HASH"  text NOT NULL,

        "file_name"    text,
        "file_size"    integer,
        "torrent_file" blob,

        "remark"    text,
        
        PRIMARY KEY ("TOR_HASH" DESC)
        );
        """;

    private static final String sqlCreateAniTorTable =
        """
        CREATE TABLE "ani_tor" (

        "ANI_ID"          integer NOT NULL,
        "TOR_HASH"        text    NOT NULL,

        "air_datetime"    text,
        "size"            integer,
        "url_page"        text,
        "title"           text,
        "subtitle_group"  text,
        "description"     text,

        "status_download" text,
        "remark"          text,
        
        PRIMARY KEY ("ANI_ID" DESC, "TOR_HASH" DESC),
        
        CONSTRAINT "ANI_ID" 
        FOREIGN KEY ("ANI_ID") 
        REFERENCES "anime" ("ANI_ID")
        ON DELETE CASCADE ON UPDATE CASCADE,

        CONSTRAINT "TOR_HASH" 
        FOREIGN KEY ("TOR_HASH") 
        REFERENCES "torrent" ("TOR_HASH") 
        ON DELETE CASCADE ON UPDATE CASCADE
        );
        """;


    /**
     * 初始化数据库，创建表格
     */
    static void initDatabase(String db_url) throws SQLException {
        System.out.println("创建数据库...");
        try(var conn = DriverManager.getConnection(db_url)) {
            conn.prepareStatement(sqlCreateAnimeTable).execute();
            conn.prepareStatement(sqlCreateEpisodeTable).execute();
            conn.prepareStatement(sqlCreateTorrentTable).execute();
            conn.prepareStatement(sqlCreateAniTorTable).execute();
        }
        System.out.println("数据库创建完成");
    }
}
