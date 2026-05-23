package Database;

import java.sql.*;
import java.time.OffsetDateTime;
import java.util.Map;

public class EpisodeRecordInfo {

    public final Integer        EPI_ID;
    public final OffsetDateTime view_datetime;

    public final Integer        rating;
    public final String         comment;


    static PreparedStatement GetUpsertStatement(Connection conn) throws SQLException {
        String upsertSqlFetch =
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
        return conn.prepareStatement(upsertSqlFetch);
    }

    void SetParams(PreparedStatement ps) throws SQLException {
        Utils.safeSetInt            (ps, 1, EPI_ID        );
        Utils.safeSetOffsetDateTime (ps, 2, view_datetime );
        Utils.safeSetInt            (ps, 3, rating       );
        Utils.safeSetString         (ps, 4, comment      );
    }


    public EpisodeRecordInfo(Map<String, String> data) {

        // 参数检查
        if(data == null || data.isEmpty()) {
            throw new IllegalArgumentException("EpisodeRecord构造函数: 传入的Map<String, String>为null或空");
        }

        // 检查
        {
            Integer epiId = null;
            var epiIdStr = data.getOrDefault("EPI_ID", null);

            if(epiIdStr == null) {
                throw new IllegalArgumentException("EpisodeRecord构造函数: EPI_ID不能为空");
            }
            try { epiId = Integer.parseInt(epiIdStr); }
            catch(NumberFormatException _) {
                throw new IllegalArgumentException("EpisodeRecord构造函数: EPI_ID必须是整数");
            }
            EPI_ID = epiId;
        }

        {
            OffsetDateTime viewDatetime = null;
            var viewDatetimeStr = data.getOrDefault("view_datetime", null);

            if(viewDatetimeStr == null) {
                throw new IllegalArgumentException("EpisodeRecord构造函数: view_datetime不能为空");
            }
            try { viewDatetime = OffsetDateTime.parse(viewDatetimeStr); }
            catch(Exception _) {
                throw new IllegalArgumentException("EpisodeRecord构造函数: view_datetime必须是ISO 8601格式的日期时间字符串");
            }

            view_datetime = viewDatetime;
        }

        {
            Integer ratingTemp = null;
            var     ratingStr  = data.getOrDefault("rating", null);
            if(ratingStr != null) {
                try { ratingTemp = Integer.parseInt(ratingStr); }
                catch(NumberFormatException _) {
                    throw new IllegalArgumentException("EpisodeRecord构造函数: rating必须是整数");
                }
            }
            rating = ratingTemp;
        }

        comment = data.getOrDefault("comment", null);
    }
}
