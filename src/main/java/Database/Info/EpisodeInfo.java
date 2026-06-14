package Database.Info;

import java.sql.*;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.Map;

import Utils.DatabaseUtils;
import Utils.UtilityFunctions;


public class EpisodeInfo extends BaseInfo {

    public final Integer        EPI_ID;
    public final Integer        ANI_ID;

    public final Integer        ep;
    public final Double         sort;
    public final Date           air_date;
    public final Integer        duration;
    public final String         title;
    public final String         title_cn;
    public final String         description;

    public final OffsetDateTime update_datetime;


    @Override
    public void setParams(PreparedStatement ps) throws SQLException {
        DatabaseUtils.safeSetInt            (ps,  1, EPI_ID         );
        DatabaseUtils.safeSetInt            (ps,  2, ANI_ID         );
        DatabaseUtils.safeSetInt            (ps,  3, ep             );
        DatabaseUtils.safeSetDouble         (ps,  4, sort           );
        DatabaseUtils.safeSetDate           (ps,  5, air_date       );
        DatabaseUtils.safeSetInt            (ps,  6, duration       );
        DatabaseUtils.safeSetString         (ps,  7, title          );
        DatabaseUtils.safeSetString         (ps,  8, title_cn       );
        DatabaseUtils.safeSetString         (ps,  9, description    );
        DatabaseUtils.safeSetOffsetDateTime (ps, 10, update_datetime);
    }

    public EpisodeInfo(
        Integer EPI_ID,
        Integer ANI_ID,
        Integer ep,
        Double  sort,
        Date    air_date,
        Integer duration,
        String  title,
        String  title_cn,
        String  description

    ) {
        // 参数检查
        String errorMsg = "";
        if (EPI_ID == null) errorMsg += "参数 'EPI_ID' 不能为空";
        if (ANI_ID == null) errorMsg += "参数 'ANI_ID' 不能为空";
        if (!errorMsg.isEmpty()) throw new IllegalArgumentException("EpisodeInfo构造函数: " + errorMsg);

        this.EPI_ID          = EPI_ID;
        this.ANI_ID          = ANI_ID;
        this.ep              = ep;
        this.sort            = sort;
        this.air_date        = air_date;
        this.duration        = duration;
        this.title           = title;
        this.title_cn        = title_cn;
        this.description     = description;
        this.update_datetime = OffsetDateTime.now();
    }

    public EpisodeInfo(Map<String, String> data) {

        // 参数检查
        if(data == null || data.isEmpty()) {
            throw new IllegalArgumentException("EpisodeInfo构造函数: 传入的Map<String, String>为null或空");
        }

        // 解析 EPI_ID
        {
            Integer parsedEpiId = null;
            var epi_id_str = data.getOrDefault("EPI_ID", null);

            if(epi_id_str == null) {
                throw new IllegalArgumentException("EpisodeInfo构造函数: Map<String, String>缺少必需的键 'EPI_ID'");
            }

            try {
                parsedEpiId = Integer.parseInt(epi_id_str);
            } catch(NumberFormatException e) {
                throw new IllegalArgumentException("EpisodeInfo构造函数: 键 'EPI_ID' 的值无法解析为整数: " + epi_id_str, e);
            }

            EPI_ID = parsedEpiId;
        }

        // 解析 ANI_ID
        {
            Integer parsedAniId = null;
            var ani_id_str = data.getOrDefault("ANI_ID", null);

            if(ani_id_str == null) {
                throw new IllegalArgumentException("EpisodeInfo构造函数: Map<String, String>缺少必需的键 'ANI_ID'");
            }

            try {
                parsedAniId = Integer.parseInt(ani_id_str);
            } catch(NumberFormatException e) {
                throw new IllegalArgumentException("EpisodeInfo构造函数: 键 'ANI_ID' 的值无法解析为整数: " + ani_id_str, e);
            }

            ANI_ID = parsedAniId;
        }

        {
            Integer parsedEp = null;
            if(data.containsKey("ep")) {
                var epStr = data.get("ep");
                if(epStr != null) try {
                    parsedEp = Integer.parseInt(epStr);
                } catch(NumberFormatException _) {}
            }
            ep = parsedEp;
        }

        {
            Double parsedSort = null;
            if(data.containsKey("sort")) {
                var sortStr = data.get("sort");
                if(sortStr != null) try {
                    parsedSort = Double.parseDouble(sortStr);
                } catch(NumberFormatException _) {}
            }
            sort = parsedSort;
        }

        air_date = UtilityFunctions.parseDate(data.getOrDefault("air_date", null));

        {
            Integer parsedDuration = null;
            if(data.containsKey("duration")) {
                var durationStr = data.get("duration");
                if(durationStr != null) try {
                    parsedDuration = Integer.parseInt(durationStr);
                } catch(NumberFormatException _) {}
            }
            duration = parsedDuration;
        }

        title       = data.getOrDefault("title", null);
        title_cn    = data.getOrDefault("title_cn", null);
        description = data.getOrDefault("description", null);

        update_datetime = OffsetDateTime.now(); // 设置更新时间为当前时间
    }


    @Override
    public String toPrintString(String indent, boolean enable_color) {
        return formatInfo("EpisodeInfo", indent, enable_color, new Object[][] {
            { "EPI_ID", EPI_ID },
            { "ANI_ID", ANI_ID },
            { "ep", ep },
            { "sort", sort },
            { "air_date", air_date },
            { "duration", duration },
            { "title", title },
            { "title_cn", title_cn },
            { "description", description },
            { "update_datetime", update_datetime }
        });
    }

    @Override
    public String toString() {
        return toPrintString("", false);
    }

    public String toFormatString() {
        return toPrintString("", false);
    }
}
