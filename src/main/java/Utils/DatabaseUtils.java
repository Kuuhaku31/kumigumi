package Utils;

import static Utils.UtilityFunctions.getDateString;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

public final class DatabaseUtils {

    private DatabaseUtils() {}

    public static void safeSetInt(PreparedStatement ps, int index, Integer value) throws SQLException {
        if(value == null) {
            ps.setNull(index, java.sql.Types.INTEGER);
        } else {
            ps.setInt(index, value);
        }
    }

    public static void safeSetLong(PreparedStatement ps, int index, Long value) throws SQLException {
        if(value == null) {
            ps.setNull(index, java.sql.Types.BIGINT);
        } else {
            ps.setLong(index, value);
        }
    }

    public static void safeSetFloat(PreparedStatement ps, int index, Float value) throws SQLException {
        if(value == null) {
            ps.setNull(index, java.sql.Types.REAL);
        } else {
            ps.setFloat(index, value);
        }
    }

    public static void safeSetDouble(PreparedStatement ps, int index, Double value) throws SQLException {
        if(value == null) {
            ps.setNull(index, java.sql.Types.DOUBLE);
        } else {
            ps.setDouble(index, value);
        }
    }

    public static void safeSetString(PreparedStatement ps, int index, String value) throws SQLException {
        if(value == null) {
            ps.setNull(index, java.sql.Types.VARCHAR);
        } else {
            ps.setString(index, value);
        }
    }

    public static void safeSetDate(PreparedStatement ps, int index, java.util.Date value) throws SQLException {
        if(value == null) {
            ps.setNull(index, java.sql.Types.DATE);
        } else {
            ps.setString(index, getDateString(value));
        }
    }

    public static void safeSetOffsetDateTime(PreparedStatement ps, int index, OffsetDateTime value) throws SQLException {
        if(value == null) {
            ps.setNull(index, java.sql.Types.TIMESTAMP);
        } else {
            ps.setString(index, getDateString(value));
        }
    }

    public static void safeSetBytes(PreparedStatement ps, int index, byte[] value) throws SQLException {
        if(value == null) {
            ps.setNull(index, java.sql.Types.BLOB);
        } else {
            ps.setBytes(index, value);
        }
    }

    public static LinkedHashSet<String> normalizeHashes(Collection<String> hashes) {
        var uniqueHashes = new LinkedHashSet<String>();
        if(hashes == null) return uniqueHashes;

        for(var hash : hashes) {
            if(hash != null && !hash.isBlank()) uniqueHashes.add(hash);
        }
        return uniqueHashes;
    }

    public static List<List<String>> chunks(Collection<String> values, int chunkSize) {
        var input = new ArrayList<>(values);
        var res   = new ArrayList<List<String>>();
        for(var start = 0; start < input.size(); start += chunkSize) {
            var end = Math.min(start + chunkSize, input.size());
            res.add(input.subList(start, end));
        }
        return res;
    }

    public static void bindStrings(PreparedStatement ps, List<String> values) throws SQLException {
        for(var i = 0; i < values.size(); i++) {
            ps.setString(i + 1, values.get(i));
        }
    }
}
