package Database.Info;

import java.sql.*;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Set;

import Utils.DatabaseUtils;
import Utils.TableData;


public class EpisodeRecordInfo extends BaseInfo {

    public final Integer        EPI_ID;
    public final OffsetDateTime view_datetime;

    public final Integer        rating;
    public final String         comment;

    @Override
    public void setParams(PreparedStatement ps) throws SQLException {
        DatabaseUtils.safeSetInt            (ps, 1, EPI_ID        );
        DatabaseUtils.safeSetOffsetDateTime (ps, 2, view_datetime );
        DatabaseUtils.safeSetInt            (ps, 3, rating       );
        DatabaseUtils.safeSetString         (ps, 4, comment      );
    }


    public EpisodeRecordInfo(Integer epi_id, OffsetDateTime view_datetime, Integer rating, String comment) {
        if(epi_id == null) {
            throw new IllegalArgumentException("EpisodeRecordInfo构造函数: EPI_ID不能为空");
        }
        if(view_datetime == null) {
            throw new IllegalArgumentException("EpisodeRecordInfo构造函数: view_datetime不能为空");
        }
        EPI_ID             = epi_id;
        this.view_datetime = view_datetime;
        this.rating        = rating;
        this.comment       = comment;
    }

    /**
     * 从Map<String, String>创建EpisodeRecordInfo实例
     * @param data
     */
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

    /**
     * 从TableData创建EpisodeRecordInfo实例
     * @param data
     */
    public static Set<EpisodeRecordInfo> ParseEpisodeRecordInfoByTableData(TableData data) {

        // 获取列索引
        var epi_id_index         = data.GetColumnIndex("EPI_ID");
        var view_datetime_index  = data.GetColumnIndex("view_datetime");
        var timezone_index       = data.GetColumnIndex("timezone");
        var rating_index         = data.GetColumnIndex("rating");
        var comment_index        = data.GetColumnIndex("comment");

        Set<EpisodeRecordInfo> infoSet = new java.util.HashSet<>();
        for(var rowIndex = 0; rowIndex < data.GetRowSize(); rowIndex++) {

            Integer        epi_id = null;
            String         timezone = null;
            OffsetDateTime view_datetime = null;
            Integer        rating = null;
            String         comment = null;
            var            row = data.GetRow(rowIndex);

            if(epi_id_index != -1) {
                try { epi_id = Integer.parseInt(row[epi_id_index]); }
                catch(NumberFormatException _) {}
            }

            if(timezone_index != -1) {
                timezone = row[timezone_index];
            }

            if(view_datetime_index != -1) {
                try { view_datetime = OffsetDateTime.parse(row[view_datetime_index] + timezone); }
                catch(Exception _) {}
            }

            if(rating_index != -1) {
                try { rating = Integer.parseInt(row[rating_index]); }
                catch(NumberFormatException _) {}
            }

            if(comment_index != -1) {
                comment = row[comment_index];
            }

            try {
                var new_info = new EpisodeRecordInfo(epi_id, view_datetime, rating, comment);
                infoSet.add(new_info);
            } catch(IllegalArgumentException _) {
                // 忽略无效的EpisodeRecordInfo对象
            }
        }
        return infoSet;
    }


    @Override
    public String toPrintString(String indent, boolean enable_color) {
        return formatInfo("EpisodeRecordInfo", indent, enable_color, new Object[][] {
            { "EPI_ID", EPI_ID },
            { "view_datetime", view_datetime },
            { "rating", rating },
            { "comment", comment }
        });
    }

    @Override
    public String toString() {
        return toPrintString("", false);
    }
}
