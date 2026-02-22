// cSpell:words jdbc

package Database;

import static util.Util.getDateString;

import Database.Item.UpdateItem;
import Database.Item.UpsertItem;
import InfoItem.InfoAni.*;
import InfoItem.InfoAniTor.*;
import InfoItem.InfoEpi.*;
import InfoItem.InfoItem;
import InfoItem.InfoTor.*;
import java.io.Closeable;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;


public class SQLiteAccess implements Closeable {

    private final Connection           conn;                     // SQLite 数据库连接
    private final SQLiteStatementCache stmtCache;                // 语句缓存持有者
    private static final int           DEFAULT_BATCH_SIZE = 500; // 默认批处理大小

    public SQLiteAccess(String dbPath) throws SQLException {

        // 确保数据库文件存在
        var dbUrl = "jdbc:sqlite:" + dbPath;
        if(!new File(dbPath).exists()) {
            System.out.println("该数据库文件不存在: " + dbPath);
            System.out.println("正在创建数据库...");
            SQLiteInit.initDatabase(dbUrl); // 如果表不存在则创建表
        }
        conn = DriverManager.getConnection(dbUrl); // 连接 SQLite 数据库

        // 应用 PRAGMA 设置以优化性能
        try(var st = conn.createStatement()) {
            st.execute("PRAGMA journal_mode = WAL;");
            st.execute("PRAGMA synchronous = NORMAL;");
            st.execute("PRAGMA temp_store = MEMORY;");
            st.execute("PRAGMA cache_size = 10000;");
        } catch(SQLException e) {
            System.err.println("Failed to apply PRAGMA settings: " + e.getMessage());
        }

        // 初始化语句缓存
        stmtCache = new SQLiteStatementCache(conn);
    }

    /**
     * 利用 UpsertItem 插入或更新项目
     * 根据 UpsertItem 的类型执行相应的插入或更新操作
     * 通过批处理提高性能
     * 在插入或更新过程中关闭自动提交，最后提交事务
     */
    public void Upsert(List<UpsertItem> items) throws SQLException {

        // 如果没有项目需要处理，则直接返回
        if(items == null || items.isEmpty()) return;

        // 关闭自动提交以启用批处理
        var prevAuto = conn.getAutoCommit(); // 保存之前的自动提交状态
        conn.setAutoCommit(false);           // 关闭自动提交

        // 依次处理每个项目，根据其类型添加到相应的批处理中
        int aniCount = 0, epiCount = 0, torCount = 0, aniTorCount = 0; // 统计每种类型的项目数量以控制批处理执行
        for(var it : items) {

            // InfoAniUpsert
            if(it instanceof InfoAniUpsert) {
                var itemInfoAniUpsert = (InfoAniUpsert)it;
                safeSetInt(stmtCache.psAniUpsert, 1, itemInfoAniUpsert.ANI_ID); // 设置 ANI_ID 以满足主键约束，但不作为更新的字段
                stmtCache.psAniUpsert.addBatch();
                aniCount++;

                // 如果达到批处理大小则执行批处理
                if(aniCount % DEFAULT_BATCH_SIZE == 0) stmtCache.psAniUpsert.executeBatch();
            }

            // InfoEpiUpsert
            else if(it instanceof InfoEpiUpsert) {
                var itemInfoEpiUpsert = (InfoEpiUpsert)it;
                safeSetInt(stmtCache.psEpiUpsert, 1, itemInfoEpiUpsert.EPI_ID); // 仅使用 EPI_ID 作为主键进行插入或更新
                safeSetInt(stmtCache.psEpiUpsert, 2, itemInfoEpiUpsert.ANI_ID); // 设置 ANI_ID 以满足外键约束，但不作为主键更新的字段
                stmtCache.psEpiUpsert.addBatch();
                epiCount++;

                // 如果达到批处理大小则执行批处理
                if(epiCount % DEFAULT_BATCH_SIZE == 0) stmtCache.psEpiUpsert.executeBatch();
            }

            // InfoTorUpsert
            else if(it instanceof InfoTorUpsert) {
                var itemInfoTorUpsert = (InfoTorUpsert)it;
                safeSetString(stmtCache.psTorUpsert, 1, itemInfoTorUpsert.TOR_HASH); // 设置 TOR_HASH 以满足主键约束
                stmtCache.psTorUpsert.addBatch();
                torCount++;

                // 如果达到批处理大小则执行批处理
                if(torCount % DEFAULT_BATCH_SIZE == 0) stmtCache.psTorUpsert.executeBatch();
            }

            // InfoAniTorUpsert
            else if(it instanceof InfoAniTorUpsert) {
                var itemInfoAniTorUpsert = (InfoAniTorUpsert)it;
                safeSetInt(stmtCache.psAniTorUpsert, 1, itemInfoAniTorUpsert.ANI_ID);      // 设置 ANI_ID 以满足外键约束，但不作为主键更新的字段
                safeSetString(stmtCache.psAniTorUpsert, 2, itemInfoAniTorUpsert.TOR_HASH); // 设置 TOR_HASH 以满足外键约束，但不作为主键更新的字段
                stmtCache.psAniTorUpsert.addBatch();
                aniTorCount++;

                // 如果达到批处理大小则执行批处理
                if(aniTorCount % DEFAULT_BATCH_SIZE == 0) stmtCache.psAniTorUpsert.executeBatch();
            }

            // 未知类型
            else {
                System.err.println("未知的插入类型: " + it.getClass().getName());
            }
        }

        // 执行剩余的批处理
        if(aniCount > 0) stmtCache.psAniUpsert.executeBatch();
        if(epiCount > 0) stmtCache.psEpiUpsert.executeBatch();
        if(torCount > 0) stmtCache.psTorUpsert.executeBatch();
        if(aniTorCount > 0) stmtCache.psAniTorUpsert.executeBatch();

        // 提交事务并恢复之前的自动提交状态
        conn.commit();
        conn.setAutoCommit(prevAuto);
    }


    /**
     * 利用 UpdateItem 更新项目
     * 根据 UpdateItem 的类型执行相应的更新操作
     * 通过批处理提高性能
     * 在更新过程中关闭自动提交，最后提交事务
     * @param items 需要更新的项目列表
     * @throws SQLException 如果数据库操作失败
     */
    public void Update(List<UpdateItem> items) throws SQLException {

        // 如果没有项目需要处理，则直接返回
        if(items == null || items.isEmpty()) return;

        // 关闭自动提交以启用批处理
        var prevAuto = conn.getAutoCommit();
        conn.setAutoCommit(false);

        // 依次处理每个项目，根据其类型添加到相应的批处理中
        int aniFetchCount = 0, aniStoreCount = 0;
        int epiFetchCount = 0, epiStoreCount = 0;
        int torFetchCount = 0, torStoreCount = 0;
        int aniTorFetchCount = 0, aniTorStoreCount = 0;
        for(var it : items) {

            // anime
            if(it instanceof InfoAniFetch) {
                var itemInfoAniFetch = (InfoAniFetch)it;

                // 使用安全的设置方法处理可能为 null 的值
                safeSetDate(stmtCache.psAniFetch, 1, itemInfoAniFetch.air_date);
                safeSetString(stmtCache.psAniFetch, 2, itemInfoAniFetch.title);
                safeSetString(stmtCache.psAniFetch, 3, itemInfoAniFetch.title_cn);
                safeSetString(stmtCache.psAniFetch, 4, itemInfoAniFetch.aliases);
                safeSetString(stmtCache.psAniFetch, 5, itemInfoAniFetch.description);
                safeSetInt(stmtCache.psAniFetch, 6, itemInfoAniFetch.episode_count);
                safeSetString(stmtCache.psAniFetch, 7, itemInfoAniFetch.url_official_site);
                safeSetString(stmtCache.psAniFetch, 8, itemInfoAniFetch.url_cover);
                safeSetInt(stmtCache.psAniFetch, 9, itemInfoAniFetch.ANI_ID);

                // 将更新添加到批处理中
                stmtCache.psAniFetch.addBatch();
                aniFetchCount++;

                // 如果达到批处理大小则执行批处理
                if(aniFetchCount % DEFAULT_BATCH_SIZE == 0) stmtCache.psAniFetch.executeBatch();

            } else if(it instanceof InfoAniStore) {
                var itemInfoAniStore = (InfoAniStore)it;

                // 使用安全的设置方法处理可能为 null 的值
                safeSetString(stmtCache.psAniStore, 1, itemInfoAniStore.url_rss);
                safeSetInt(stmtCache.psAniStore, 2, itemInfoAniStore.rating_before);
                safeSetInt(stmtCache.psAniStore, 3, itemInfoAniStore.rating_after);
                safeSetString(stmtCache.psAniStore, 4, itemInfoAniStore.remark);
                safeSetInt(stmtCache.psAniStore, 5, itemInfoAniStore.ANI_ID);

                // 将更新添加到批处理中
                stmtCache.psAniStore.addBatch();
                aniStoreCount++;

                // 如果达到批处理大小则执行批处理
                if(aniStoreCount % DEFAULT_BATCH_SIZE == 0) stmtCache.psAniStore.executeBatch();
            }

            // episode
            else if(it instanceof InfoEpiFetch) {
                var itemInfoEpiFetch = (InfoEpiFetch)it;

                // 使用安全的设置方法处理可能为 null 的值
                safeSetInt(stmtCache.psEpiFetch, 1, itemInfoEpiFetch.ep);
                safeSetDouble(stmtCache.psEpiFetch, 2, itemInfoEpiFetch.sort);
                safeSetDate(stmtCache.psEpiFetch, 3, itemInfoEpiFetch.air_date);
                safeSetInt(stmtCache.psEpiFetch, 4, itemInfoEpiFetch.duration);
                safeSetString(stmtCache.psEpiFetch, 5, itemInfoEpiFetch.title);
                safeSetString(stmtCache.psEpiFetch, 6, itemInfoEpiFetch.title_cn);
                safeSetString(stmtCache.psEpiFetch, 7, itemInfoEpiFetch.description);
                safeSetInt(stmtCache.psEpiFetch, 8, itemInfoEpiFetch.EPI_ID);

                // 将更新添加到批处理中
                stmtCache.psEpiFetch.addBatch();
                epiFetchCount++;

                // 如果达到批处理大小则执行批处理
                if(epiFetchCount % DEFAULT_BATCH_SIZE == 0) stmtCache.psEpiFetch.executeBatch();

            } else if(it instanceof InfoEpiStore) {
                var itemInfoEpiStore = (InfoEpiStore)it;

                // 使用安全的设置方法处理可能为 null 的值
                safeSetInt(stmtCache.psEpiStore, 1, itemInfoEpiStore.rating);
                safeSetOffsetDateTime(stmtCache.psEpiStore, 2, itemInfoEpiStore.view_datetime);
                safeSetString(stmtCache.psEpiStore, 3, itemInfoEpiStore.status_download);
                safeSetString(stmtCache.psEpiStore, 4, itemInfoEpiStore.status_view);
                safeSetString(stmtCache.psEpiStore, 5, itemInfoEpiStore.remark);
                safeSetInt(stmtCache.psEpiStore, 6, itemInfoEpiStore.EPI_ID);

                // 将更新添加到批处理中
                stmtCache.psEpiStore.addBatch();
                epiStoreCount++;

                // 如果达到批处理大小则执行批处理
                if(epiStoreCount % DEFAULT_BATCH_SIZE == 0) stmtCache.psEpiStore.executeBatch();
            }

            // torrent
            else if(it instanceof InfoTorFetch) {
                var itemInfoTorFetch = (InfoTorFetch)it;

                // 使用安全的设置方法处理可能为 null 的值
                safeSetString(stmtCache.psTorFetch, 1, itemInfoTorFetch.file_name);
                safeSetInt(stmtCache.psTorFetch, 2, itemInfoTorFetch.file_size);
                safeSetBytes(stmtCache.psTorFetch, 3, itemInfoTorFetch.file);
                safeSetString(stmtCache.psTorFetch, 4, itemInfoTorFetch.TOR_HASH);

                // 将更新添加到批处理中
                stmtCache.psTorFetch.addBatch();
                torFetchCount++;

                // 如果达到批处理大小则执行批处理
                if(torFetchCount % DEFAULT_BATCH_SIZE == 0) stmtCache.psTorFetch.executeBatch();

            } else if(it instanceof InfoTorStore) {
                var itemInfoTorStore = (InfoTorStore)it;

                // 使用安全的设置方法处理可能为 null 的值
                safeSetString(stmtCache.psTorStore, 1, itemInfoTorStore.remark);
                safeSetString(stmtCache.psTorStore, 2, itemInfoTorStore.TOR_HASH);

                // 将更新添加到批处理中
                stmtCache.psTorStore.addBatch();
                torStoreCount++;

                // 如果达到批处理大小则执行批处理
                if(torStoreCount % DEFAULT_BATCH_SIZE == 0) stmtCache.psTorStore.executeBatch();
            }

            // anime-torrent 关联项
            else if(it instanceof InfoAniTorFetch) {
                var itemInfoAniTorFetch = (InfoAniTorFetch)it;

                // 使用安全的设置方法处理可能为 null 的值
                safeSetOffsetDateTime(stmtCache.psAniTorFetch, 1, itemInfoAniTorFetch.air_datetime);
                safeSetString(stmtCache.psAniTorFetch, 2, itemInfoAniTorFetch.url_page);
                safeSetString(stmtCache.psAniTorFetch, 3, itemInfoAniTorFetch.title);
                safeSetString(stmtCache.psAniTorFetch, 4, itemInfoAniTorFetch.subtitle_group);
                safeSetString(stmtCache.psAniTorFetch, 5, itemInfoAniTorFetch.description);
                safeSetString(stmtCache.psAniTorFetch, 6, itemInfoAniTorFetch.TOR_HASH);

                // 将更新添加到批处理中
                stmtCache.psAniTorFetch.addBatch();
                aniTorFetchCount++;

                // 如果达到批处理大小则执行批处理
                if(aniTorFetchCount % DEFAULT_BATCH_SIZE == 0) stmtCache.psAniTorFetch.executeBatch();
            } else if(it instanceof InfoAniTorStore) {
                var itemInfoAniTorStore = (InfoAniTorStore)it;

                // 使用安全的设置方法处理可能为 null 的值
                safeSetString(stmtCache.psAniTorStore, 1, itemInfoAniTorStore.status_download);
                safeSetString(stmtCache.psAniTorStore, 2, itemInfoAniTorStore.remark);
                safeSetString(stmtCache.psAniTorStore, 3, itemInfoAniTorStore.TOR_HASH);

                // 将更新添加到批处理中
                stmtCache.psAniTorStore.addBatch();
                aniTorStoreCount++;

                // 如果达到批处理大小则执行批处理
                if(aniTorStoreCount % DEFAULT_BATCH_SIZE == 0) stmtCache.psAniTorStore.executeBatch();
            }

            // 未知类型
            else {
                System.err.println("未知的更新类型: " + it.getClass().getName());
            }
        }

        // 执行剩余的批处理
        if(aniFetchCount > 0) stmtCache.psAniFetch.executeBatch();
        if(aniStoreCount > 0) stmtCache.psAniStore.executeBatch();
        if(epiFetchCount > 0) stmtCache.psEpiFetch.executeBatch();
        if(epiStoreCount > 0) stmtCache.psEpiStore.executeBatch();
        if(torFetchCount > 0) stmtCache.psTorFetch.executeBatch();
        if(torStoreCount > 0) stmtCache.psTorStore.executeBatch();
        if(aniTorFetchCount > 0) stmtCache.psAniTorFetch.executeBatch();
        if(aniTorStoreCount > 0) stmtCache.psAniTorStore.executeBatch();

        // 提交事务并恢复之前的自动提交状态
        conn.commit();
        conn.setAutoCommit(prevAuto);
    }

    /**
     * 删除项目
     */
    public void Delete(List<InfoItem> item) throws SQLException {

        // 如果没有项目需要处理，则直接返回
        if(item == null || item.isEmpty()) return;

        // 关闭自动提交以启用批处理
        var prevAuto = conn.getAutoCommit();
        conn.setAutoCommit(false);

        // 依次处理每个项目，根据其类型添加到相应的批处理中
        int aniCount = 0, epiCount = 0, torCount = 0, aniTorCount = 0; // 统计每种类型的项目数量以控制批处理执行
        for(var it : item) {

            // anime
            if(it instanceof InfoAni) {
                var infoAni = (InfoAni)it;
                safeSetInt(stmtCache.psAniDelete, 1, infoAni.ANI_ID);
                stmtCache.psAniDelete.addBatch();
                aniCount++;
                if(aniCount % DEFAULT_BATCH_SIZE == 0) stmtCache.psAniDelete.executeBatch();
            }

            // episode
            else if(it instanceof InfoEpi) {
                var infoEpi = (InfoEpi)it;
                safeSetInt(stmtCache.psEpiDelete, 1, infoEpi.EPI_ID);
                stmtCache.psEpiDelete.addBatch();
                epiCount++;
                if(epiCount % DEFAULT_BATCH_SIZE == 0) stmtCache.psEpiDelete.executeBatch();
            }

            // torrent
            else if(it instanceof InfoTor) {
                var infoTor = (InfoTor)it;
                safeSetString(stmtCache.psTorDelete, 1, infoTor.TOR_HASH);
                stmtCache.psTorDelete.addBatch();
                torCount++;
                if(torCount % DEFAULT_BATCH_SIZE == 0) stmtCache.psTorDelete.executeBatch();
            }

            // anime-torrent 关联项
            else if(it instanceof InfoAniTor) {
                var infoAniTor = (InfoAniTor)it;
                safeSetInt(stmtCache.psAniTorDelete, 1, infoAniTor.ANI_ID);
                safeSetString(stmtCache.psAniTorDelete, 2, infoAniTor.TOR_HASH);
                stmtCache.psAniTorDelete.addBatch();
                aniTorCount++;
                if(aniTorCount % DEFAULT_BATCH_SIZE == 0) stmtCache.psAniTorDelete.executeBatch();
            }

            // 未知类型
            else {
                System.err.println("未知的删除类型: " + it.getClass().getName());
            }
        }

        // 执行剩余的批处理
        if(aniCount > 0) stmtCache.psAniDelete.executeBatch();
        if(epiCount > 0) stmtCache.psEpiDelete.executeBatch();
        if(torCount > 0) stmtCache.psTorDelete.executeBatch();
        if(aniTorCount > 0) stmtCache.psAniTorDelete.executeBatch();

        // 提交事务并恢复之前的自动提交状态
        conn.commit();
        conn.setAutoCommit(prevAuto);
    }

    /**
     * 关闭数据库连接和语句缓存
     * 在关闭过程中捕获并打印任何异常，以确保资源得到正确释放
     */
    @Override
    public void close() {
        if(stmtCache != null) {
            try {
                stmtCache.close();
            } catch(Exception e) {
                System.err.println("Failed to close statement cache: " + e.getMessage());
            }
        }

        if(conn != null) {
            try {
                conn.close();
            } catch(SQLException e) {
                System.err.println("Close failed: " + e.getMessage());
            }
        }
    }


    private static void safeSetInt(PreparedStatement ps, int index, Integer value) throws SQLException {
        if(value == null) {
            ps.setNull(index, java.sql.Types.INTEGER);
        } else {
            ps.setInt(index, value);
        }
    }

    private static void safeSetDouble(PreparedStatement ps, int index, Float value) throws SQLException {
        if(value == null) {
            ps.setNull(index, java.sql.Types.REAL);
        } else {
            ps.setDouble(index, value);
        }
    }

    private static void safeSetString(PreparedStatement ps, int index, String value) throws SQLException {
        if(value == null) {
            ps.setNull(index, java.sql.Types.VARCHAR);
        } else {
            ps.setString(index, value);
        }
    }

    private static void safeSetDate(PreparedStatement ps, int index, java.util.Date value) throws SQLException {
        if(value == null) {
            ps.setNull(index, java.sql.Types.DATE);
        } else {
            ps.setString(index, getDateString(value));
        }
    }

    private static void safeSetOffsetDateTime(PreparedStatement ps, int index, OffsetDateTime value) throws SQLException {
        if(value == null) {
            ps.setNull(index, java.sql.Types.TIMESTAMP);
        } else {
            ps.setString(index, getDateString(value));
        }
    }

    private static void safeSetBytes(PreparedStatement ps, int index, byte[] value) throws SQLException {
        if(value == null) {
            ps.setNull(index, java.sql.Types.BLOB);
        } else {
            ps.setBytes(index, value);
        }
    }
}
