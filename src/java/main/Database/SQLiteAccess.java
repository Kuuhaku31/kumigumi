package Database;

import java.io.Closeable;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import Database.InfoItem.InfoItem;
import Database.InfoItem.InfoAni.InfoAni;
import Database.InfoItem.InfoEpi.InfoEpi;
import Database.InfoItem.InfoTor.InfoTor;
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
            var i = 1;
            ps.setInt(i++, item.ANI_ID);
            ps.setString(i++, getDateString(item.air_date));
            ps.setString(i++, item.title);
            ps.setString(i++, item.title_cn);
            ps.setString(i++, item.aliases);
            ps.setString(i++, item.description);
            ps.setInt(i++, item.episode_count);
            ps.setString(i++, item.url_official_site);
            ps.setString(i++, item.url_cover);
            ps.executeUpdate();
        }
    }

    /** 插入或更新单个 InfoAniStore 项目 */
    public void Update(InfoAniStore item) throws SQLException {
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
            var i = 1;
            ps.setInt(i++, item.ANI_ID);
            ps.setString(i++, item.url_rss);
            ps.setInt(i++, item.rating_before);
            ps.setInt(i++, item.rating_after);
            ps.setString(i++, item.remark);
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
            var i = 1;
            ps.setInt(i++, item.EPI_ID);
            ps.setInt(i++, item.ANI_ID);
            ps.setInt(i++, item.ep);
            ps.setDouble(i++, item.sort);
            ps.setString(i++, getDateString(item.air_date));
            ps.setInt(i++, item.duration);
            ps.setString(i++, item.title);
            ps.setString(i++, item.title_cn);
            ps.setString(i++, item.description);
            ps.executeUpdate();
        }
    }

    /** 插入或更新单个 InfoEpiStore 项目 */
    public void Update(InfoEpiStore item) throws SQLException {
        final String sqlEpiStore = """
                UPDATE episode
                SET
                    rating          = ?,
                    view_datetime   = ?,
                    status_download = ?,
                    status_view     = ?,
                    remark          = ?
                WHERE EPI_ID = ?;
                """;
        try (var ps = conn.prepareStatement(sqlEpiStore)) {
            var i = 1;
            ps.setInt(i++, item.rating);
            ps.setString(i++, getDateString(item.view_datetime));
            ps.setString(i++, item.status_download);
            ps.setString(i++, item.status_view);
            ps.setString(i++, item.remark);
            ps.setInt(i++, item.EPI_ID);
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
            var i = 1;
            ps.setString(i++, item.TOR_URL);
            ps.setInt(i++, item.ANI_ID);
            ps.setString(i++, getDateString(item.air_datetime));
            ps.setLong(i++, item.size);
            ps.setString(i++, item.url_page);
            ps.setString(i++, item.title);
            ps.setString(i++, item.subtitle_group);
            ps.setString(i++, item.description);
            ps.executeUpdate();
        }
    }

    /** 插入或更新单个 InfoTorStore 项目 */
    public void Update(InfoTorStore item) throws SQLException {
        final String sqlTorStore = """
                UPDATE torrent
                SET
                    status_download = ?,
                    remark          = ?
                WHERE TOR_URL = ?;
                """;
        try (var ps = conn.prepareStatement(sqlTorStore)) {
            var i = 1;
            ps.setString(i++, item.TOR_URL);
            ps.setString(i++, item.status_download);
            ps.setString(i++, item.remark);
            ps.executeUpdate();
        }
    }

    /** 删除项目 */
    public void Delete(InfoItem item) throws SQLException {
        String sqlDelete = null;
        if (item instanceof InfoAni) {
            sqlDelete = "DELETE FROM anime WHERE ANI_ID = ?;";
        } else if (item instanceof InfoEpi) {
            sqlDelete = "DELETE FROM episode WHERE EPI_ID = ?;";
        } else if (item instanceof InfoTor) {
            sqlDelete = "DELETE FROM torrent WHERE TOR_URL = ?;";
        }

        if (sqlDelete != null) {
            try (var ps = conn.prepareStatement(sqlDelete)) {
                if (item instanceof InfoAni) {
                    ps.setInt(1, ((InfoAni) item).ANI_ID);
                } else if (item instanceof InfoEpi) {
                    ps.setInt(1, ((InfoEpi) item).EPI_ID);
                } else if (item instanceof InfoTor) {
                    ps.setString(1, ((InfoTor) item).TOR_URL);
                }
                ps.executeUpdate();
            }
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
