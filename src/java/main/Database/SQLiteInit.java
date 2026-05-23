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

    "update_datetime" text NOT NULL,

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

    "update_datetime" text NOT NULL,

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

    "TOR_HASH"     text NOT NULL,

    "file_name"    text,
    "file_size"    integer,
    "torrent_file" blob,

    PRIMARY KEY ("TOR_HASH" DESC)
    );
    """;

    private static final String sqlCreateTorrentPageTable =
    """
    CREATE TABLE "torrent_page" (

    "URL_RSS"         text NOT NULL,
    "TOR_HASH"        text NOT NULL,

    "air_datetime"    text,
    "url_download"    text,
    "url_page"        text,
    "title"           text,
    "subtitle_group"  text,
    "description"     text,

    "update_datetime" text NOT NULL,

    PRIMARY KEY ("URL_RSS" DESC, "TOR_HASH" DESC),

    CONSTRAINT "URL_RSS"
    FOREIGN KEY ("URL_RSS")
    REFERENCES "rss" ("URL_RSS")
    ON DELETE CASCADE ON UPDATE CASCADE
    );
    """;

    private static final String sqlCreateRSSTable =
    """
    CREATE TABLE "rss" (

    "URL_RSS" text NOT NULL,
    "ANI_ID"  integer,

    PRIMARY KEY ("URL_RSS" DESC),

    CONSTRAINT "ANI_ID"
    FOREIGN KEY ("ANI_ID")
    REFERENCES "anime" ("ANI_ID")
    ON DELETE SET NULL ON UPDATE CASCADE
    );
    """;

    private static final String sqlCreateEpisodeRecordTable =
    """
    CREATE TABLE "episode_record" (

    "EPI_ID"        integer NOT NULL,
    "view_datetime" text NOT NULL,

    "rating"  integer,
    "comment" text,

    PRIMARY KEY ("EPI_ID" DESC, "view_datetime" DESC),

    CONSTRAINT "EPI_ID"
    FOREIGN KEY ("EPI_ID")
    REFERENCES "episode" ("EPI_ID")
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
            conn.prepareStatement(sqlCreateTorrentPageTable).execute();
            conn.prepareStatement(sqlCreateRSSTable).execute();
            conn.prepareStatement(sqlCreateEpisodeRecordTable).execute();
        }
        System.out.println("数据库创建完成");
    }
}
