package Database;

import java.util.Collections;
import java.util.List;

import Database.Info.BaseInfo;
import Database.Info.RSSInfo;
import Database.Info.TorrentInfo;
import Database.Info.TorrentPageInfo;
import Database.Info.AnimeInfo;
import Database.Info.EpisodeInfo;
import Database.Info.EpisodeRecordInfo;

final class SQLiteSQL {

    private SQLiteSQL() {}

    static final List<String> PRAGMA_SETTINGS = List.of(
        "PRAGMA foreign_keys = ON;",
        "PRAGMA journal_mode = WAL;",
        "PRAGMA synchronous = NORMAL;",
        "PRAGMA temp_store = MEMORY;",
        "PRAGMA cache_size = 10000;"
    );

    static final String CREATE_ANIME_TABLE =
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

    PRIMARY KEY ("ANI_ID" DESC)
    );
    """;

    static final String CREATE_EPISODE_TABLE =
    """
    CREATE TABLE "episode" (

    "EPI_ID"          integer NOT NULL,
    "ANI_ID"          integer NOT NULL,

    "ep"              integer,
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

    static final String CREATE_TORRENT_TABLE =
    """
    CREATE TABLE "torrent" (

    "TOR_HASH"     text NOT NULL,

    "file_name"    text,
    "file_size"    integer,
    "torrent_file" blob,

    PRIMARY KEY ("TOR_HASH" DESC)
    );
    """;

    static final String CREATE_TORRENT_PAGE_TABLE =
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

    static final String CREATE_RSS_TABLE =
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

    static final String CREATE_EPISODE_RECORD_TABLE =
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

    static final String UPSERT_ANIME_INFO =
    """
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
    ON CONFLICT(ANI_ID) DO UPDATE SET
        air_date            = excluded.air_date,
        title               = excluded.title,
        title_cn            = excluded.title_cn,
        aliases             = excluded.aliases,
        description         = excluded.description,
        episode_count       = excluded.episode_count,
        url_official_site   = excluded.url_official_site,
        url_cover           = excluded.url_cover;
    """;

    static final String UPSERT_EPISODE_INFO =
    """
    INSERT INTO episode (
        EPI_ID,
        ANI_ID,
        ep,
        sort,
        air_date,
        duration,
        title,
        title_cn,
        description,
        update_datetime
    )
    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    ON CONFLICT(EPI_ID) DO UPDATE SET
        ANI_ID              = excluded.ANI_ID,
        ep                  = excluded.ep,
        sort                = excluded.sort,
        air_date            = excluded.air_date,
        duration            = excluded.duration,
        title               = excluded.title,
        title_cn            = excluded.title_cn,
        description         = excluded.description,
        update_datetime     = excluded.update_datetime;
    """;

    static final String UPSERT_EPISODE_RECORD_INFO =
    """
    INSERT INTO episode_record (
        EPI_ID,
        view_datetime,
        rating,
        comment
    )
    VALUES (?, ?, ?, ?)
    ON CONFLICT(EPI_ID, view_datetime) DO UPDATE SET
        rating      = excluded.rating,
        comment     = excluded.comment;
    """;

    static final String UPSERT_RSS_INFO =
    """
    INSERT INTO rss (
        URL_RSS,
        ANI_ID
    )
    VALUES (?, ?)
    ON CONFLICT(URL_RSS) DO UPDATE SET
        ANI_ID = excluded.ANI_ID;
    """;

    static final String UPSERT_TORRENT_PAGE_INFO =
    """
    INSERT INTO torrent_page (
        URL_RSS,
        TOR_HASH,
        air_datetime,
        url_download,
        url_page,
        title,
        subtitle_group,
        description,
        update_datetime
    )
    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
    ON CONFLICT(URL_RSS, TOR_HASH) DO UPDATE SET
        air_datetime      = excluded.air_datetime,
        url_download      = excluded.url_download,
        url_page          = excluded.url_page,
        title             = excluded.title,
        subtitle_group    = excluded.subtitle_group,
        description       = excluded.description,
        update_datetime   = excluded.update_datetime;
    """;

    static final String UPSERT_TORRENT_INFO =
    """
    INSERT INTO torrent (
        TOR_HASH,
        file_name,
        file_size,
        torrent_file
    )
    VALUES (?, ?, ?, ?)
    ON CONFLICT(TOR_HASH) DO UPDATE SET
        file_name    = excluded.file_name,
        file_size    = excluded.file_size,
        torrent_file = excluded.torrent_file;
    """;

    static List<String> createTableStatements() {
        return List.of(
            CREATE_ANIME_TABLE,
            CREATE_EPISODE_TABLE,
            CREATE_EPISODE_RECORD_TABLE,
            CREATE_RSS_TABLE,
            CREATE_TORRENT_TABLE,
            CREATE_TORRENT_PAGE_TABLE
        );
    }

    static String upsertInfo(Class<? extends BaseInfo> infoType) {
        if(infoType == AnimeInfo.class)         return UPSERT_ANIME_INFO;
        if(infoType == EpisodeInfo.class)       return UPSERT_EPISODE_INFO;
        if(infoType == EpisodeRecordInfo.class) return UPSERT_EPISODE_RECORD_INFO;
        if(infoType == RSSInfo.class)           return UPSERT_RSS_INFO;
        if(infoType == TorrentPageInfo.class)   return UPSERT_TORRENT_PAGE_INFO;
        if(infoType == TorrentInfo.class)       return UPSERT_TORRENT_INFO;

        throw new IllegalArgumentException("Unsupported Info type: " + infoType.getName());
    }

    static String selectTorrentFilesByHashCount(int count) {
        return "SELECT TOR_HASH, torrent_file FROM torrent WHERE TOR_HASH IN (" + placeholders(count) + ")";
    }

    static String selectExistingTorrentHashesByHashCount(int count) {
        return "SELECT TOR_HASH FROM torrent "
        + "WHERE TOR_HASH IN (" + placeholders(count) + ") "
        + "AND torrent_file IS NOT NULL "
        + "AND length(torrent_file) > 0";
    }

    static String selectDownloadUrlsByHashCount(int count) {
        return "SELECT TOR_HASH, url_download FROM torrent_page WHERE TOR_HASH IN (" + placeholders(count) + ")";
    }

    private static String placeholders(int count) {
        return String.join(",", Collections.nCopies(count, "?"));
    }
}
