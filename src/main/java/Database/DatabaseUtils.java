package Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import Info.AnimeInfo;
import Info.EpisodeInfo;
import Info.EpisodeRecordInfo;
import Info.RSSInfo;
import Info.TorrentInfo;
import Info.TorrentPageInfo;

import static Utils.UtilityFunctions.getDateString;


final class DatabaseUtils {

    private DatabaseUtils() {}

    static void safeSetInt(PreparedStatement ps, int index, Integer value) throws SQLException {
        if(value == null) {
            ps.setNull(index, java.sql.Types.INTEGER);
        } else {
            ps.setInt(index, value);
        }
    }

    static void safeSetLong(PreparedStatement ps, int index, Long value) throws SQLException {
        if(value == null) {
            ps.setNull(index, java.sql.Types.BIGINT);
        } else {
            ps.setLong(index, value);
        }
    }

    static void safeSetFloat(PreparedStatement ps, int index, Float value) throws SQLException {
        if(value == null) {
            ps.setNull(index, java.sql.Types.REAL);
        } else {
            ps.setFloat(index, value);
        }
    }

    static void safeSetDouble(PreparedStatement ps, int index, Double value) throws SQLException {
        if(value == null) {
            ps.setNull(index, java.sql.Types.DOUBLE);
        } else {
            ps.setDouble(index, value);
        }
    }

    static void safeSetString(PreparedStatement ps, int index, String value) throws SQLException {
        if(value == null) {
            ps.setNull(index, java.sql.Types.VARCHAR);
        } else {
            ps.setString(index, value);
        }
    }

    static void safeSetDate(PreparedStatement ps, int index, java.util.Date value) throws SQLException {
        if(value == null) {
            ps.setNull(index, java.sql.Types.DATE);
        } else {
            ps.setString(index, getDateString(value));
        }
    }

    static void safeSetOffsetDateTime(PreparedStatement ps, int index, OffsetDateTime value) throws SQLException {
        if(value == null) {
            ps.setNull(index, java.sql.Types.TIMESTAMP);
        } else {
            ps.setString(index, getDateString(value));
        }
    }

    static void safeSetBytes(PreparedStatement ps, int index, byte[] value) throws SQLException {
        if(value == null) {
            ps.setNull(index, java.sql.Types.BLOB);
        } else {
            ps.setBytes(index, value);
        }
    }

    static LinkedHashSet<String> normalizeHashes(Collection<String> hashes) {
        var uniqueHashes = new LinkedHashSet<String>();
        if(hashes == null) return uniqueHashes;

        for(var hash : hashes) {
            if(hash != null && !hash.isBlank()) uniqueHashes.add(hash);
        }
        return uniqueHashes;
    }

    static List<List<String>> chunks(Collection<String> values, int chunkSize) {
        var input = new ArrayList<>(values);
        var res   = new ArrayList<List<String>>();
        for(var start = 0; start < input.size(); start += chunkSize) {
            var end = Math.min(start + chunkSize, input.size());
            res.add(input.subList(start, end));
        }
        return res;
    }

    static void bindStrings(PreparedStatement ps, List<String> values) throws SQLException {
        for(var i = 0; i < values.size(); i++) {
            ps.setString(i + 1, values.get(i));
        }
    }

    static void initialize_database_schema(Connection conn) throws SQLException {
        var prev_auto = conn.getAutoCommit();
        conn.setAutoCommit(false);
        try(var st = conn.createStatement()) {
            for(var sql : SQLiteSQL.createTableStatements()) st.execute(sql);
            for(var sql : SQLiteSQL.createViewStatements()) st.execute(sql);
            conn.commit();
        }
        catch(SQLException | RuntimeException e) { conn.rollback(); throw e; }
        finally { conn.setAutoCommit(prev_auto); }
    }

    private record SchemaObject(String type, String sql) {}

    static void validate_database_schema(Connection conn) throws SQLException {
        Map<String, SchemaObject> expected;
        try(var expected_conn = DriverManager.getConnection("jdbc:sqlite::memory:")) {
            initialize_database_schema(expected_conn);
            expected = read_schema_objects(expected_conn);
        }

        var actual = read_schema_objects(conn);
        var errors = new ArrayList<String>();
        for(var entry : expected.entrySet()) {
            var name = entry.getKey();
            var expected_object = entry.getValue();
            var actual_object = actual.get(name);

            if(actual_object == null) {
                errors.add("缺失 " + expected_object.type() + ": " + name);
            } else if(!expected_object.type().equals(actual_object.type())) {
                errors.add("对象类型错误: " + name + "，预期 " + expected_object.type() + "，实际 " + actual_object.type());
            } else if(!normalize_schema_sql(expected_object.sql()).equals(normalize_schema_sql(actual_object.sql()))) {
                errors.add("结构不正确 " + expected_object.type() + ": " + name);
            }
        }

        if(!errors.isEmpty()) {
            throw new SQLException("数据库结构校验失败:\n- " + String.join("\n- ", errors));
        }
    }

       private static Map<String, SchemaObject> read_schema_objects(Connection conn) throws SQLException {
        Map<String, SchemaObject> result = new LinkedHashMap<>();
        try(var st = conn.createStatement();
            var rs = st.executeQuery(SQLiteSQL.SELECT_SCHEMA_OBJECTS)) {
            while(rs.next()) {
                result.put(rs.getString("name"), new SchemaObject(rs.getString("type"), rs.getString("sql")));
            }
        }
        return result;
    }

    private static String normalize_schema_sql(String sql) {
        if(sql == null) return "";
        return sql.replaceAll("\\s+", "").toLowerCase(Locale.ROOT);
    }

    static void set_params_anime_info(PreparedStatement ps, AnimeInfo info) throws SQLException {
        DatabaseUtils.safeSetInt            (ps,  1, info.ANI_ID           );
        DatabaseUtils.safeSetDate           (ps,  2, info.air_date         );
        DatabaseUtils.safeSetString         (ps,  3, info.title            );
        DatabaseUtils.safeSetString         (ps,  4, info.title_cn         );
        DatabaseUtils.safeSetString         (ps,  5, info.aliases          );
        DatabaseUtils.safeSetString         (ps,  6, info.description      );
        DatabaseUtils.safeSetInt            (ps,  7, info.episode_count    );
        DatabaseUtils.safeSetString         (ps,  8, info.url_official_site);
        DatabaseUtils.safeSetString         (ps,  9, info.url_cover        );
        DatabaseUtils.safeSetOffsetDateTime (ps, 10, info.update_datetime  );
    }

    static void set_params_episode_info(PreparedStatement ps, EpisodeInfo info) throws SQLException {
        DatabaseUtils.safeSetInt            (ps,  1, info.EPI_ID         );
        DatabaseUtils.safeSetInt            (ps,  2, info.ANI_ID         );
        DatabaseUtils.safeSetInt            (ps,  3, info.ep             );
        DatabaseUtils.safeSetDouble         (ps,  4, info.sort           );
        DatabaseUtils.safeSetDate           (ps,  5, info.air_date       );
        DatabaseUtils.safeSetInt            (ps,  6, info.duration       );
        DatabaseUtils.safeSetString         (ps,  7, info.title          );
        DatabaseUtils.safeSetString         (ps,  8, info.title_cn       );
        DatabaseUtils.safeSetString         (ps,  9, info.description    );
        DatabaseUtils.safeSetOffsetDateTime (ps, 10, info.update_datetime);
    }

    static void set_params_episode_record_info(PreparedStatement ps, EpisodeRecordInfo info) throws SQLException {
        DatabaseUtils.safeSetInt            (ps, 1, info.EPI_ID       );
        DatabaseUtils.safeSetOffsetDateTime (ps, 2, info.view_datetime);
        DatabaseUtils.safeSetInt            (ps, 3, info.rating       );
        DatabaseUtils.safeSetString         (ps, 4, info.comment      );
    }

    static void set_params_rss_info(PreparedStatement ps, RSSInfo info) throws SQLException {
        DatabaseUtils.safeSetString (ps, 1, info.URL_RSS);
        DatabaseUtils.safeSetInt    (ps, 2, info.ANI_ID );
    }

    static void set_params_torrent_page_info(PreparedStatement ps, TorrentPageInfo info) throws SQLException {
        DatabaseUtils.safeSetString         (ps, 1, info.URL_RSS        );
        DatabaseUtils.safeSetString         (ps, 2, info.TOR_HASH       );
        DatabaseUtils.safeSetOffsetDateTime (ps, 3, info.air_datetime   );
        DatabaseUtils.safeSetString         (ps, 4, info.url_download   );
        DatabaseUtils.safeSetString         (ps, 5, info.url_page       );
        DatabaseUtils.safeSetString         (ps, 6, info.title          );
        DatabaseUtils.safeSetString         (ps, 7, info.subtitle_group );
        DatabaseUtils.safeSetString         (ps, 8, info.description    );
        DatabaseUtils.safeSetOffsetDateTime (ps, 9, info.update_datetime);
    }

    static void set_params_torrent_info(PreparedStatement ps, TorrentInfo info) throws SQLException {
        DatabaseUtils.safeSetString (ps, 1, info.TOR_HASH    );
        DatabaseUtils.safeSetString (ps, 2, info.file_name   );
        DatabaseUtils.safeSetLong   (ps, 3, info.file_size   );
        DatabaseUtils.safeSetBytes  (ps, 4, info.torrent_file);
    }
}
