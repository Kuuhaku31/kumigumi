// cSpell:words jdbc

package Database;

import Database.Item.UpdateItem;
import Database.Item.UpsertItem;
import InfoItem.InfoAni.*;
import InfoItem.InfoAniTor.*;
import InfoItem.InfoEpi.*;
import InfoItem.InfoItem;
import InfoItem.InfoTor.*;

import static Util.Util.getDateString;

import java.io.Closeable;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;


public class SQLiteAccess implements Closeable {

    private final Connection           conn;                     // SQLite 数据库连接
    private final SQLiteStatementCache statementCache;           // 语句缓存持有者
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
        statementCache = new SQLiteStatementCache(conn);
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
            if(it instanceof InfoAni) {
                var itemInfoAniUpsert = (InfoAni)it;
                safeSetInt(statementCache.psAniUpsert, 1, itemInfoAniUpsert.ANI_ID); // 设置 ANI_ID 以满足主键约束，但不作为更新的字段
                statementCache.psAniUpsert.addBatch();
                aniCount++;

                // 如果达到批处理大小则执行批处理
                if(aniCount % DEFAULT_BATCH_SIZE == 0) statementCache.psAniUpsert.executeBatch();
            }

            // InfoEpiUpsert
            else if(it instanceof InfoEpi) {
                var itemInfoEpiUpsert = (InfoEpi)it;
                safeSetInt(statementCache.psEpiUpsert, 1, itemInfoEpiUpsert.EPI_ID); // 仅使用 EPI_ID 作为主键进行插入或更新
                safeSetInt(statementCache.psEpiUpsert, 2, itemInfoEpiUpsert.ANI_ID); // 设置 ANI_ID 以满足外键约束，但不作为主键更新的字段
                statementCache.psEpiUpsert.addBatch();
                epiCount++;

                // 如果达到批处理大小则执行批处理
                if(epiCount % DEFAULT_BATCH_SIZE == 0) statementCache.psEpiUpsert.executeBatch();
            }

            // InfoTorUpsert
            else if(it instanceof InfoTor) {
                var itemInfoTorUpsert = (InfoTor)it;
                safeSetString(statementCache.psTorUpsert, 1, itemInfoTorUpsert.TOR_HASH); // 设置 TOR_HASH 以满足主键约束
                statementCache.psTorUpsert.addBatch();
                torCount++;

                // 如果达到批处理大小则执行批处理
                if(torCount % DEFAULT_BATCH_SIZE == 0) statementCache.psTorUpsert.executeBatch();
            }

            // InfoAniTorUpsert
            else if(it instanceof InfoAniTor) {
                var itemInfoAniTorUpsert = (InfoAniTor)it;

                if(itemInfoAniTorUpsert.TOR_HASH.startsWith("http")) {
                    System.err.println("Warning: InfoAniTorUpsert with null TOR_HASH for ANI_ID: " + itemInfoAniTorUpsert.ANI_ID);
                }

                safeSetInt(statementCache.psAniTorUpsert, 1, itemInfoAniTorUpsert.ANI_ID);      // 设置 ANI_ID 以满足外键约束，但不作为主键更新的字段
                safeSetString(statementCache.psAniTorUpsert, 2, itemInfoAniTorUpsert.TOR_HASH); // 设置 TOR_HASH 以满足外键约束，但不作为主键更新的字段
                statementCache.psAniTorUpsert.addBatch();
                aniTorCount++;

                // 如果达到批处理大小则执行批处理
                if(aniTorCount % DEFAULT_BATCH_SIZE == 0) statementCache.psAniTorUpsert.executeBatch();
            }

            // 未知类型
            else {
                System.err.println("未知的插入类型: " + it.getClass().getName());
            }
        }

        // 执行剩余的批处理
        if(aniCount > 0) statementCache.psAniUpsert.executeBatch();
        if(epiCount > 0) statementCache.psEpiUpsert.executeBatch();
        if(torCount > 0) statementCache.psTorUpsert.executeBatch();
        if(aniTorCount > 0) statementCache.psAniTorUpsert.executeBatch();

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
        int aniFetchCount    = 0, aniStoreCount    = 0;
        int epiFetchCount    = 0, epiStoreCount    = 0;
        int torFetchCount    = 0, torStoreCount    = 0;
        int aniTorFetchCount = 0, aniTorStoreCount = 0;
        for(var it : items) {

            // anime
            if(it instanceof InfoAniFetch) {
                var itemInfoAniFetch = (InfoAniFetch)it;

                // 使用安全的设置方法处理可能为 null 的值
                safeSetDate(    statementCache.psAniFetch, 1, itemInfoAniFetch.air_date         );
                safeSetString(  statementCache.psAniFetch, 2, itemInfoAniFetch.title            );
                safeSetString(  statementCache.psAniFetch, 3, itemInfoAniFetch.title_cn         );
                safeSetString(  statementCache.psAniFetch, 4, itemInfoAniFetch.aliases          );
                safeSetString(  statementCache.psAniFetch, 5, itemInfoAniFetch.description      );
                safeSetInt(     statementCache.psAniFetch, 6, itemInfoAniFetch.episode_count    );
                safeSetString(  statementCache.psAniFetch, 7, itemInfoAniFetch.url_official_site);
                safeSetString(  statementCache.psAniFetch, 8, itemInfoAniFetch.url_cover        );
                safeSetInt(     statementCache.psAniFetch, 9, itemInfoAniFetch.ANI_ID           );

                // 将更新添加到批处理中
                statementCache.psAniFetch.addBatch();
                aniFetchCount++;

                // 如果达到批处理大小则执行批处理
                if(aniFetchCount % DEFAULT_BATCH_SIZE == 0) statementCache.psAniFetch.executeBatch();
            }
            else if(it instanceof InfoAniStore) {
                var itemInfoAniStore = (InfoAniStore)it;

                // 使用安全的设置方法处理可能为 null 的值
                safeSetString(  statementCache.psAniStore, 1, itemInfoAniStore.url_rss          );
                safeSetInt(     statementCache.psAniStore, 2, itemInfoAniStore.rating_before    );
                safeSetInt(     statementCache.psAniStore, 3, itemInfoAniStore.rating_after     );
                safeSetString(  statementCache.psAniStore, 4, itemInfoAniStore.remark           );
                safeSetInt(     statementCache.psAniStore, 5, itemInfoAniStore.ANI_ID           );

                // 将更新添加到批处理中
                statementCache.psAniStore.addBatch();
                aniStoreCount++;

                // 如果达到批处理大小则执行批处理
                if(aniStoreCount % DEFAULT_BATCH_SIZE == 0) statementCache.psAniStore.executeBatch();
            }

            // episode
            else if(it instanceof InfoEpiFetch) {
                var itemInfoEpiFetch = (InfoEpiFetch)it;

                // 使用安全的设置方法处理可能为 null 的值
                safeSetInt(statementCache.psEpiFetch, 1, itemInfoEpiFetch.ep);
                safeSetDouble(statementCache.psEpiFetch, 2, itemInfoEpiFetch.sort);
                safeSetDate(statementCache.psEpiFetch, 3, itemInfoEpiFetch.air_date);
                safeSetInt(statementCache.psEpiFetch, 4, itemInfoEpiFetch.duration);
                safeSetString(statementCache.psEpiFetch, 5, itemInfoEpiFetch.title);
                safeSetString(statementCache.psEpiFetch, 6, itemInfoEpiFetch.title_cn);
                safeSetString(statementCache.psEpiFetch, 7, itemInfoEpiFetch.description);
                safeSetInt(statementCache.psEpiFetch, 8, itemInfoEpiFetch.EPI_ID);

                // 将更新添加到批处理中
                statementCache.psEpiFetch.addBatch();
                epiFetchCount++;

                // 如果达到批处理大小则执行批处理
                if(epiFetchCount % DEFAULT_BATCH_SIZE == 0) statementCache.psEpiFetch.executeBatch();
            }
            else if(it instanceof InfoEpiStore) {
                var itemInfoEpiStore = (InfoEpiStore)it;

                // 使用安全的设置方法处理可能为 null 的值
                safeSetInt(statementCache.psEpiStore, 1, itemInfoEpiStore.rating);
                safeSetOffsetDateTime(statementCache.psEpiStore, 2, itemInfoEpiStore.view_datetime);
                safeSetString(statementCache.psEpiStore, 3, itemInfoEpiStore.status_download);
                safeSetString(statementCache.psEpiStore, 4, itemInfoEpiStore.status_view);
                safeSetString(statementCache.psEpiStore, 5, itemInfoEpiStore.remark);
                safeSetInt(statementCache.psEpiStore, 6, itemInfoEpiStore.EPI_ID);

                // 将更新添加到批处理中
                statementCache.psEpiStore.addBatch();
                epiStoreCount++;

                // 如果达到批处理大小则执行批处理
                if(epiStoreCount % DEFAULT_BATCH_SIZE == 0) statementCache.psEpiStore.executeBatch();
            }

            // torrent
            else if(it instanceof InfoTorFetch) {
                var itemInfoTorFetch = (InfoTorFetch)it;

                // 使用安全的设置方法处理可能为 null 的值
                safeSetString(statementCache.psTorFetch, 1, itemInfoTorFetch.file_name);
                safeSetLong(statementCache.psTorFetch, 2, itemInfoTorFetch.file_size);
                safeSetBytes(statementCache.psTorFetch, 3, itemInfoTorFetch.file);
                safeSetString(statementCache.psTorFetch, 4, itemInfoTorFetch.TOR_HASH);

                // 将更新添加到批处理中
                statementCache.psTorFetch.addBatch();
                torFetchCount++;

                // 如果达到批处理大小则执行批处理
                if(torFetchCount % DEFAULT_BATCH_SIZE == 0) statementCache.psTorFetch.executeBatch();

            } else if(it instanceof InfoTorStore) {
                var itemInfoTorStore = (InfoTorStore)it;

                // 使用安全的设置方法处理可能为 null 的值
                safeSetString(statementCache.psTorStore, 1, itemInfoTorStore.remark);
                safeSetString(statementCache.psTorStore, 2, itemInfoTorStore.TOR_HASH);

                // 将更新添加到批处理中
                statementCache.psTorStore.addBatch();
                torStoreCount++;

                // 如果达到批处理大小则执行批处理
                if(torStoreCount % DEFAULT_BATCH_SIZE == 0) statementCache.psTorStore.executeBatch();
            }

            // anime-torrent 关联项
            else if(it instanceof InfoAniTorFetch) {
                var itemInfoAniTorFetch = (InfoAniTorFetch)it;

                // 使用安全的设置方法处理可能为 null 的值
                safeSetOffsetDateTime(statementCache.psAniTorFetch, 1, itemInfoAniTorFetch.air_datetime  );
                safeSetString        (statementCache.psAniTorFetch, 2, itemInfoAniTorFetch.url_download  );
                safeSetString        (statementCache.psAniTorFetch, 3, itemInfoAniTorFetch.url_page      );
                safeSetString        (statementCache.psAniTorFetch, 4, itemInfoAniTorFetch.title         );
                safeSetString        (statementCache.psAniTorFetch, 5, itemInfoAniTorFetch.subtitle_group);
                safeSetString        (statementCache.psAniTorFetch, 6, itemInfoAniTorFetch.description   );

                safeSetInt           (statementCache.psAniTorFetch, 7, itemInfoAniTorFetch.ANI_ID        );
                safeSetString        (statementCache.psAniTorFetch, 8, itemInfoAniTorFetch.TOR_HASH      );

                // 将更新添加到批处理中
                statementCache.psAniTorFetch.addBatch();
                aniTorFetchCount++;

                // 如果达到批处理大小则执行批处理
                if(aniTorFetchCount % DEFAULT_BATCH_SIZE == 0) statementCache.psAniTorFetch.executeBatch();
            } else if(it instanceof InfoAniTorStore) {
                var itemInfoAniTorStore = (InfoAniTorStore)it;

                // 使用安全的设置方法处理可能为 null 的值
                safeSetString(statementCache.psAniTorStore, 1, itemInfoAniTorStore.status_download);
                safeSetString(statementCache.psAniTorStore, 2, itemInfoAniTorStore.remark         );

                safeSetInt   (statementCache.psAniTorStore, 3, itemInfoAniTorStore.ANI_ID         );
                safeSetString(statementCache.psAniTorStore, 4, itemInfoAniTorStore.TOR_HASH       );

                // 将更新添加到批处理中
                statementCache.psAniTorStore.addBatch();
                aniTorStoreCount++;

                // 如果达到批处理大小则执行批处理
                if(aniTorStoreCount % DEFAULT_BATCH_SIZE == 0) statementCache.psAniTorStore.executeBatch();
            }

            // 未知类型
            else {
                System.err.println("未知的更新类型: " + it.getClass().getName());
            }
        }

        // 执行剩余的批处理
        if(aniFetchCount > 0) statementCache.psAniFetch.executeBatch();
        if(aniStoreCount > 0) statementCache.psAniStore.executeBatch();
        if(epiFetchCount > 0) statementCache.psEpiFetch.executeBatch();
        if(epiStoreCount > 0) statementCache.psEpiStore.executeBatch();
        if(torFetchCount > 0) statementCache.psTorFetch.executeBatch();
        if(torStoreCount > 0) statementCache.psTorStore.executeBatch();
        if(aniTorFetchCount > 0) statementCache.psAniTorFetch.executeBatch();
        if(aniTorStoreCount > 0) statementCache.psAniTorStore.executeBatch();

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
                safeSetInt(statementCache.psAniDelete, 1, infoAni.ANI_ID);
                statementCache.psAniDelete.addBatch();
                aniCount++;
                if(aniCount % DEFAULT_BATCH_SIZE == 0) statementCache.psAniDelete.executeBatch();
            }

            // episode
            else if(it instanceof InfoEpi) {
                var infoEpi = (InfoEpi)it;
                safeSetInt(statementCache.psEpiDelete, 1, infoEpi.EPI_ID);
                statementCache.psEpiDelete.addBatch();
                epiCount++;
                if(epiCount % DEFAULT_BATCH_SIZE == 0) statementCache.psEpiDelete.executeBatch();
            }

            // torrent
            else if(it instanceof InfoTor) {
                var infoTor = (InfoTor)it;
                safeSetString(statementCache.psTorDelete, 1, infoTor.TOR_HASH);
                statementCache.psTorDelete.addBatch();
                torCount++;
                if(torCount % DEFAULT_BATCH_SIZE == 0) statementCache.psTorDelete.executeBatch();
            }

            // anime-torrent 关联项
            else if(it instanceof InfoAniTor) {
                var infoAniTor = (InfoAniTor)it;
                safeSetInt(statementCache.psAniTorDelete, 1, infoAniTor.ANI_ID);
                safeSetString(statementCache.psAniTorDelete, 2, infoAniTor.TOR_HASH);
                statementCache.psAniTorDelete.addBatch();
                aniTorCount++;
                if(aniTorCount % DEFAULT_BATCH_SIZE == 0) statementCache.psAniTorDelete.executeBatch();
            }

            // 未知类型
            else {
                System.err.println("未知的删除类型: " + it.getClass().getName());
            }
        }

        // 执行剩余的批处理
        if(aniCount > 0) statementCache.psAniDelete.executeBatch();
        if(epiCount > 0) statementCache.psEpiDelete.executeBatch();
        if(torCount > 0) statementCache.psTorDelete.executeBatch();
        if(aniTorCount > 0) statementCache.psAniTorDelete.executeBatch();

        // 提交事务并恢复之前的自动提交状态
        conn.commit();
        conn.setAutoCommit(prevAuto);
    }

    /**
     * 获取数据库中不存在的 infoAniTorFetchList 列表
     * @param infoAniTorFetchList
     * @return
     * @throws SQLException
     */
    public List<InfoAniTorFetch> getTorrentHashNotExist(List<InfoAniTorFetch> infoAniTorFetchList) throws SQLException {
        if(infoAniTorFetchList == null || infoAniTorFetchList.isEmpty()) return List.of();

        var uniqueHashes = new LinkedHashSet<String>();
        for(var info : infoAniTorFetchList) {
            var hash = info.TOR_HASH;
            if(hash != null && !hash.isBlank()) uniqueHashes.add(hash);
        }
        if(uniqueHashes.isEmpty()) return List.of();

        var inputList   = new ArrayList<>(uniqueHashes);
        var hasFileSet  = new HashSet<String>();
        var chunkSize   = 500; // 避免 SQLite 单条语句参数过多

        for(int start = 0; start < inputList.size(); start += chunkSize) {
            int end = Math.min(start + chunkSize, inputList.size());
            var chunk = inputList.subList(start, end);

            var placeholders = String.join(",", java.util.Collections.nCopies(chunk.size(), "?"));
            var sql 
            = "SELECT TOR_HASH FROM torrent "
            + "WHERE TOR_HASH IN (" + placeholders + ") "
            + "AND torrent_file IS NOT NULL "
            + "AND length(torrent_file) > 0";

            try(PreparedStatement ps = conn.prepareStatement(sql)) {
                for(int i = 0; i < chunk.size(); i++) {
                    ps.setString(i + 1, chunk.get(i));
                }

                try(ResultSet rs = ps.executeQuery()) {
                    while(rs.next()) {
                        hasFileSet.add(rs.getString("TOR_HASH"));
                    }
                }
            }
        }

        // 过滤出数据库中不存在的 TOR_HASH 对应的 InfoAniTorFetch 项目
        List<InfoAniTorFetch> notExistList = new ArrayList<>();
        for(var info : infoAniTorFetchList) {
            if(!hasFileSet.contains(info.TOR_HASH)) notExistList.add(info);
        }

        return notExistList;
    }

    /**
     * 根据给定的 TOR_HASH 列表从数据库中查询对应的 torrent_file 字段，并将其保存为 .torrent 文件到指定路径
     * @param torHashList
     * @param safePath
     */
    public void exportTorrentFiles(List<String> torHashList, String safePath) {
        if(torHashList == null || torHashList.isEmpty()) return;

        var placeholders = String.join(",", java.util.Collections.nCopies(torHashList.size(), "?"));
        var sql = "SELECT TOR_HASH, torrent_file FROM torrent WHERE TOR_HASH IN (" + placeholders + ")";

        try(PreparedStatement ps = conn.prepareStatement(sql)) {
            for(int i = 0; i < torHashList.size(); i++) {
                ps.setString(i + 1, torHashList.get(i));
            }

            try(ResultSet rs = ps.executeQuery()) {
                while(rs.next()) {
                    var torHash = rs.getString("TOR_HASH");
                    var fileBytes = rs.getBytes("torrent_file");
                    if(fileBytes != null && fileBytes.length > 0) {
                        var filePath = safePath + File.separator + torHash + ".torrent";
                        try(var fos = new java.io.FileOutputStream(filePath)) {
                            fos.write(fileBytes);
                        } catch(java.io.IOException e) {
                            System.err.println("Failed to save torrent file for hash: " + torHash + ", error: " + e.getMessage());
                        }
                    }
                }
            }
        } catch(SQLException e) {
            System.err.println("Failed to query torrent files: " + e.getMessage());
        }
    }

    /**
     * 关闭数据库连接和语句缓存
     * 在关闭过程中捕获并打印任何异常，以确保资源得到正确释放
     */
    @Override
    public void close() {
        if(statementCache != null) {
            try {
                statementCache.close();
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

    private static void safeSetLong(PreparedStatement ps, int index, Long value) throws SQLException {
        if(value == null) {
            ps.setNull(index, java.sql.Types.BIGINT);
        } else {
            ps.setLong(index, value);
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
