package Database;

import java.io.Closeable;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import Database.InfoItem.InfoItem;
import Database.InfoItem.UpdateItem;
import Database.InfoItem.UpsertItem;
import Database.InfoItem.InfoAni.*;
import Database.InfoItem.InfoEpi.*;
import Database.InfoItem.InfoTor.*;

import static util.Util.getDateString;

public class SQLiteAccess implements Closeable {

    private final Connection conn; // SQLite 数据库连接
    private final SQLiteStatementCache stmtCache; // 语句缓存持有者
    private static final int DEFAULT_BATCH_SIZE = 500; // 默认批处理大小

    public SQLiteAccess(String dbPath) throws SQLException {

        // 确保数据库文件存在
        var dbUrl = "jdbc:sqlite:" + dbPath;
        if (!new File(dbPath).exists()) {
            System.out.println("Database file not found.");
            SQLiteInit.initDatabase(dbUrl); // 如果表不存在则创建表
        }
        conn = DriverManager.getConnection(dbUrl); // 连接 SQLite 数据库

        // 应用 PRAGMA 设置以优化性能
        try (var st = conn.createStatement()) {
            st.execute("PRAGMA journal_mode = WAL;");
            st.execute("PRAGMA synchronous = NORMAL;");
            st.execute("PRAGMA temp_store = MEMORY;");
            st.execute("PRAGMA cache_size = 10000;");
        } catch (SQLException e) {
            System.err.println("Failed to apply PRAGMA settings: " + e.getMessage());
        }

        // 初始化语句缓存
        stmtCache = new SQLiteStatementCache(conn);
    }

    public void Upsert(List<UpsertItem> items) throws SQLException {
        if (items == null || items.isEmpty())
            return;

        var prevAuto = conn.getAutoCommit(); // 保存之前的自动提交状态
        conn.setAutoCommit(false); // 关闭自动提交

        int aniCount = 0, epiCount = 0, torCount = 0; // 计数器
        for (var it : items) {
            if (it instanceof InfoAniUpsert) {
                var item = (InfoAniUpsert) it;
                var i = 1;
                stmtCache.psAniUpsert.setInt(i++, item.ANI_ID);
                stmtCache.psAniUpsert.addBatch();
                aniCount++;
                if (aniCount % DEFAULT_BATCH_SIZE == 0) // 如果达到批处理大小则执行批处理
                    stmtCache.psAniUpsert.executeBatch();
            } else if (it instanceof InfoEpiUpsert) {
                var item = (InfoEpiUpsert) it;
                var i = 1;
                stmtCache.psEpiUpsert.setInt(i++, item.EPI_ID);
                stmtCache.psEpiUpsert.setInt(i++, item.ANI_ID);
                stmtCache.psEpiUpsert.addBatch();
                epiCount++;
                if (epiCount % DEFAULT_BATCH_SIZE == 0)
                    stmtCache.psEpiUpsert.executeBatch();
            } else if (it instanceof InfoTorUpsert) {
                var item = (InfoTorUpsert) it;
                var i = 1;
                stmtCache.psTorUpsert.setString(i++, item.TOR_URL);
                stmtCache.psTorUpsert.setInt(i++, item.ANI_ID);
                stmtCache.psTorUpsert.addBatch();
                torCount++;
                if (torCount % DEFAULT_BATCH_SIZE == 0)
                    stmtCache.psTorUpsert.executeBatch();
            } else {
                System.err.println("Unknown UpsertItem type: " + it.getClass().getName());
            }
        }

        if (aniCount > 0)
            stmtCache.psAniUpsert.executeBatch();
        if (epiCount > 0)
            stmtCache.psEpiUpsert.executeBatch();
        if (torCount > 0)
            stmtCache.psTorUpsert.executeBatch();

        conn.commit();
        conn.setAutoCommit(prevAuto);
    }

    public void Update(List<UpdateItem> items) throws SQLException {
        if (items == null || items.isEmpty())
            return;

        var prevAuto = conn.getAutoCommit();
        conn.setAutoCommit(false);

        int aniFetchCount = 0, aniStoreCount = 0;
        int epiFetchCount = 0, epiStoreCount = 0;
        int torFetchCount = 0, torStoreCount = 0;
        for (var it : items) {
            if (it instanceof InfoAniFetch) {
                var item = (InfoAniFetch) it;
                var i = 1;
                stmtCache.psAniFetch.setString(i++, getDateString(item.air_date));
                stmtCache.psAniFetch.setString(i++, item.title);
                stmtCache.psAniFetch.setString(i++, item.title_cn);
                stmtCache.psAniFetch.setString(i++, item.aliases);
                stmtCache.psAniFetch.setString(i++, item.description);
                stmtCache.psAniFetch.setInt(i++, item.episode_count);
                stmtCache.psAniFetch.setString(i++, item.url_official_site);
                stmtCache.psAniFetch.setString(i++, item.url_cover);
                stmtCache.psAniFetch.setInt(i++, item.ANI_ID);
                stmtCache.psAniFetch.addBatch();
                aniFetchCount++;
                if (aniFetchCount % DEFAULT_BATCH_SIZE == 0)
                    stmtCache.psAniFetch.executeBatch();
            } else if (it instanceof InfoEpiFetch) {
                var item = (InfoEpiFetch) it;
                var i = 1;
                if (item.ep == null)
                    stmtCache.psEpiFetch.setNull(i++, java.sql.Types.INTEGER);
                else
                    stmtCache.psEpiFetch.setInt(i++, item.ep);
                if (item.sort == null)
                    stmtCache.psEpiFetch.setNull(i++, java.sql.Types.REAL);
                else
                    stmtCache.psEpiFetch.setDouble(i++, item.sort);
                stmtCache.psEpiFetch.setString(i++, getDateString(item.air_date));
                if (item.duration == null)
                    stmtCache.psEpiFetch.setNull(i++, java.sql.Types.INTEGER);
                else
                    stmtCache.psEpiFetch.setInt(i++, item.duration);
                stmtCache.psEpiFetch.setString(i++, item.title);
                stmtCache.psEpiFetch.setString(i++, item.title_cn);
                stmtCache.psEpiFetch.setString(i++, item.description);
                stmtCache.psEpiFetch.setInt(i++, item.EPI_ID);
                stmtCache.psEpiFetch.addBatch();
                epiFetchCount++;
                if (epiFetchCount % DEFAULT_BATCH_SIZE == 0)
                    stmtCache.psEpiFetch.executeBatch();
            } else if (it instanceof InfoTorFetch) {
                var item = (InfoTorFetch) it;
                var i = 1;
                stmtCache.psTorFetch.setString(i++, getDateString(item.air_datetime));
                if (item.size == null)
                    stmtCache.psTorFetch.setNull(i++, java.sql.Types.BIGINT);
                else
                    stmtCache.psTorFetch.setLong(i++, item.size);
                stmtCache.psTorFetch.setString(i++, item.url_page);
                stmtCache.psTorFetch.setString(i++, item.title);
                stmtCache.psTorFetch.setString(i++, item.subtitle_group);
                stmtCache.psTorFetch.setString(i++, item.description);
                stmtCache.psTorFetch.setString(i++, item.TOR_URL);
                stmtCache.psTorFetch.addBatch();
                torFetchCount++;
                if (torFetchCount % DEFAULT_BATCH_SIZE == 0)
                    stmtCache.psTorFetch.executeBatch();
            } else if (it instanceof InfoAniStore) {
                var item = (InfoAniStore) it;
                var i = 1;
                stmtCache.psAniStore.setString(i++, item.url_rss);
                if (item.rating_before == null)
                    stmtCache.psAniStore.setNull(i++, java.sql.Types.INTEGER);
                else
                    stmtCache.psAniStore.setInt(i++, item.rating_before);
                if (item.rating_after == null)
                    stmtCache.psAniStore.setNull(i++, java.sql.Types.INTEGER);
                else
                    stmtCache.psAniStore.setInt(i++, item.rating_after);
                stmtCache.psAniStore.setString(i++, item.remark);
                stmtCache.psAniStore.setInt(i++, item.ANI_ID);
                stmtCache.psAniStore.addBatch();
                aniStoreCount++;
                if (aniStoreCount % DEFAULT_BATCH_SIZE == 0)
                    stmtCache.psAniStore.executeBatch();
            } else if (it instanceof InfoEpiStore) {
                var item = (InfoEpiStore) it;
                var i = 1;
                if (item.rating == null)
                    stmtCache.psEpiStore.setNull(i++, java.sql.Types.INTEGER);
                else
                    stmtCache.psEpiStore.setInt(i++, item.rating);
                stmtCache.psEpiStore.setString(i++, getDateString(item.view_datetime));
                stmtCache.psEpiStore.setString(i++, item.status_download);
                stmtCache.psEpiStore.setString(i++, item.status_view);
                stmtCache.psEpiStore.setString(i++, item.remark);
                stmtCache.psEpiStore.setInt(i++, item.EPI_ID);
                stmtCache.psEpiStore.addBatch();
                epiStoreCount++;
                if (epiStoreCount % DEFAULT_BATCH_SIZE == 0)
                    stmtCache.psEpiStore.executeBatch();
            } else if (it instanceof InfoTorStore) {
                var item = (InfoTorStore) it;
                var i = 1;
                stmtCache.psTorStore.setString(i++, item.status_download);
                stmtCache.psTorStore.setString(i++, item.remark);
                stmtCache.psTorStore.setString(i++, item.TOR_URL);
                stmtCache.psTorStore.addBatch();
                torStoreCount++;
                if (torStoreCount % DEFAULT_BATCH_SIZE == 0)
                    stmtCache.psTorStore.executeBatch();
            } else
                System.err.println("Unknown UpdateItem type: " + it.getClass().getName());
        }

        if (aniFetchCount > 0)
            stmtCache.psAniFetch.executeBatch();
        if (aniStoreCount > 0)
            stmtCache.psAniStore.executeBatch();
        if (epiFetchCount > 0)
            stmtCache.psEpiFetch.executeBatch();
        if (epiStoreCount > 0)
            stmtCache.psEpiStore.executeBatch();
        if (torFetchCount > 0)
            stmtCache.psTorFetch.executeBatch();
        if (torStoreCount > 0)
            stmtCache.psTorStore.executeBatch();

        conn.commit();
        conn.setAutoCommit(prevAuto);
    }

    /** 删除项目 */
    public void Delete(List<InfoItem> item) throws SQLException {
        if (item == null || item.isEmpty())
            return;

        var prevAuto = conn.getAutoCommit();
        conn.setAutoCommit(false);

        int aniCount = 0, epiCount = 0, torCount = 0;
        for (var it : item) {
            if (it instanceof InfoAni) {
                var info = (InfoAni) it;
                stmtCache.psAniDelete.setInt(1, info.ANI_ID);
                stmtCache.psAniDelete.addBatch();
                aniCount++;
                if (aniCount % DEFAULT_BATCH_SIZE == 0)
                    stmtCache.psAniDelete.executeBatch();
            } else if (it instanceof InfoEpi) {
                var info = (InfoEpi) it;
                stmtCache.psEpiDelete.setInt(1, info.EPI_ID);
                stmtCache.psEpiDelete.addBatch();
                epiCount++;
                if (epiCount % DEFAULT_BATCH_SIZE == 0)
                    stmtCache.psEpiDelete.executeBatch();
            } else if (it instanceof InfoTor) {
                var info = (InfoTor) it;
                stmtCache.psTorDelete.setString(1, info.TOR_URL);
                stmtCache.psTorDelete.addBatch();
                torCount++;
                if (torCount % DEFAULT_BATCH_SIZE == 0)
                    stmtCache.psTorDelete.executeBatch();
            } else {
                System.err.println("Unknown InfoItem type for deletion: " + it.getClass().getName());
            }
        }

        if (aniCount > 0)
            stmtCache.psAniDelete.executeBatch();
        if (epiCount > 0)
            stmtCache.psEpiDelete.executeBatch();
        if (torCount > 0)
            stmtCache.psTorDelete.executeBatch();

        conn.commit();
        conn.setAutoCommit(prevAuto);
    }

    @Override
    public void close() {
        if (stmtCache != null) {
            try {
                stmtCache.close();
            } catch (Exception e) {
                System.err.println("Failed to close statement cache: " + e.getMessage());
            }
        }

        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.err.println("Close failed: " + e.getMessage());
            }
        }
    }
}
