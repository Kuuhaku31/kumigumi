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
import Database.InfoItem.InfoAni.InfoAniStore;
import Database.InfoItem.InfoEpi.InfoEpiFetch;
import Database.InfoItem.InfoEpi.InfoEpiStore;
import Database.InfoItem.InfoTor.InfoTorFetch;
import Database.InfoItem.InfoTor.InfoTorStore;

import static util.Util.getDateString;

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

    /** 插入或更新单个 InfoAniFetch 项目 */
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
            ps.setString(2, getDateString(item.air_date));
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

    /** 插入或更新单个 InfoAniStore 项目 */
    public void Upsert(InfoAniStore item) throws SQLException {
        final String sqlAniStore = """
                INSERT INTO anime (
                    ANI_ID,
                    url_rss,
                    rating_before,
                    rating_after,
                    remark
                )
                VALUES (?, ?, ?, ?, ?)
                ON CONFLICT (ANI_ID) DO UPDATE SET
                    url_rss         = excluded.url_rss,
                    rating_before   = excluded.rating_before,
                    rating_after    = excluded.rating_after,
                    remark          = excluded.remark;
                """;
        try (PreparedStatement ps = conn.prepareStatement(sqlAniStore)) {
            ps.setInt(1, item.ANI_ID);
            ps.setString(2, item.url_rss);
            ps.setInt(3, item.rating_before);
            ps.setInt(4, item.rating_after);
            ps.setString(5, item.remark);
            ps.executeUpdate();
        }
    }

    /** 插入或更新单个 InfoEpiFetch 项目 */
    public void Upsert(InfoEpiFetch item) throws SQLException {
        final String sqlEpiFetch = """
                INSERT INTO episode (
                    EPI_ID,
                    ANI_ID,
                    ep,
                    sort,
                    air_date,
                    duration,
                    title,
                    title_cn,
                    description
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (EPI_ID) DO UPDATE SET
                    ANI_ID      = excluded.ANI_ID,
                    ep          = excluded.ep,
                    sort        = excluded.sort,
                    air_date    = excluded.air_date,
                    duration    = excluded.duration,
                    title       = excluded.title,
                    title_cn    = excluded.title_cn,
                    description = excluded.description;
                """;
        try (var ps = conn.prepareStatement(sqlEpiFetch)) {
            ps.setInt(1, item.EPI_ID);
            ps.setInt(2, item.ANI_ID);
            ps.setInt(3, item.ep);
            ps.setDouble(4, item.sort);
            ps.setString(5, getDateString(item.air_date));
            ps.setInt(6, item.duration);
            ps.setString(7, item.title);
            ps.setString(8, item.title_cn);
            ps.setString(9, item.description);
            ps.executeUpdate();
        }
    }

    /** 插入或更新单个 InfoEpiStore 项目 */
    public void Upsert(InfoEpiStore item) throws SQLException {
        final String sqlEpiStore = """
                INSERT INTO episode (
                    EPI_ID,
                    ANI_ID,
                    rating,
                    view_datetime,
                    status_download,
                    status_view,
                    remark
                )
                VALUES (?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (EPI_ID) DO UPDATE SET
                    ANI_ID            = excluded.ANI_ID,
                    rating            = excluded.rating,
                    view_datetime     = excluded.view_datetime,
                    status_download   = excluded.status_download,
                    status_view       = excluded.status_view,
                    remark            = excluded.remark;
                """;
        try (var ps = conn.prepareStatement(sqlEpiStore)) {
            ps.setInt(1, item.EPI_ID);
            ps.setInt(2, item.ANI_ID);
            ps.setInt(3, item.rating);
            ps.setString(4, item.view_datetime != null ? item.view_datetime.toString() : null);
            ps.setString(5, item.status_download);
            ps.setString(6, item.status_view);
            ps.setString(7, item.remark);
            ps.executeUpdate();
        }
    }

    /** 插入或更新单个 InfoTorFetch 项目 */
    public void Upsert(InfoTorFetch item) throws SQLException {
        final String sqlTorFetch = """
                INSERT INTO torrent (
                    TOR_URL,
                    ANI_ID,
                    air_datetime,
                    size,
                    url_page,
                    title,
                    subtitle_group,
                    description
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (TOR_URL) DO UPDATE SET
                    ANI_ID          = excluded.ANI_ID,
                    air_datetime    = excluded.air_datetime,
                    size            = excluded.size,
                    url_page        = excluded.url_page,
                    title           = excluded.title,
                    subtitle_group  = excluded.subtitle_group,
                    description     = excluded.description;
                """;
        try (var ps = conn.prepareStatement(sqlTorFetch)) {
            ps.setString(1, item.TOR_URL);
            ps.setInt(2, item.ANI_ID);
            ps.setString(3, getDateString(item.air_datetime));
            ps.setLong(4, item.size);
            ps.setString(5, item.url_page);
            ps.setString(6, item.title);
            ps.setString(7, item.subtitle_group);
            ps.setString(8, item.description);
            ps.executeUpdate();
        }
    }

    /** 插入或更新单个 InfoTorStore 项目 */
    public void Upsert(InfoTorStore item) throws SQLException {
        final String sqlTorStore = """
                INSERT INTO torrent (
                    TOR_URL,
                    ANI_ID,
                    status_download,
                    remark
                )
                VALUES (?, ?, ?, ?)
                ON CONFLICT (TOR_URL) DO UPDATE SET
                    ANI_ID           = excluded.ANI_ID,
                    status_download  = excluded.status_download,
                    remark           = excluded.remark;
                """;
        try (var ps = conn.prepareStatement(sqlTorStore)) {
            ps.setString(1, item.TOR_URL);
            ps.setInt(2, item.ANI_ID);
            ps.setString(3, item.status_download);
            ps.setString(4, item.remark);
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
