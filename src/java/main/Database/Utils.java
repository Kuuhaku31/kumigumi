package Database;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.OffsetDateTime;

import static Util.Util.getDateString;

class Utils {

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
}
