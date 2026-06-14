package Database.Info;

import java.sql.*;
import java.text.ParseException;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

import Excel.TableData;
import Utils.DatabaseUtils;
import Utils.UtilityFunctions;


public class AnimeInfo extends BaseInfo {

    public final Integer        ANI_ID;

    public final Date           air_date;
    public final String         title;
    public final String         title_cn;
    public final String         aliases;
    public final String         description;
    public final Integer        episode_count;
    public final String         url_official_site;
    public final String         url_cover;

    public final OffsetDateTime update_datetime;


    @Override
    public void setParams(PreparedStatement ps) throws SQLException {
        DatabaseUtils.safeSetInt            (ps,  1, ANI_ID           );
        DatabaseUtils.safeSetDate           (ps,  2, air_date         );
        DatabaseUtils.safeSetString         (ps,  3, title            );
        DatabaseUtils.safeSetString         (ps,  4, title_cn         );
        DatabaseUtils.safeSetString         (ps,  5, aliases          );
        DatabaseUtils.safeSetString         (ps,  6, description      );
        DatabaseUtils.safeSetInt            (ps,  7, episode_count    );
        DatabaseUtils.safeSetString         (ps,  8, url_official_site);
        DatabaseUtils.safeSetString         (ps,  9, url_cover        );
        DatabaseUtils.safeSetOffsetDateTime (ps, 10, update_datetime  );
    }

    public AnimeInfo(
        Integer ANI_ID,
        Date    air_date,
        String  title,
        String  title_cn,
        String  aliases,
        String  description,
        Integer episode_count,
        String  url_official_site,
        String  url_cover
    ) {
        // 参数检查
        if(ANI_ID == null) throw new IllegalArgumentException("AnimeInfo构造函数: 参数 'ANI_ID' 不能为空");

        this.ANI_ID            = ANI_ID;
        this.air_date          = air_date;
        this.title             = title;
        this.title_cn          = title_cn;
        this.aliases           = aliases;
        this.description       = description;
        this.episode_count     = episode_count;
        this.url_official_site = url_official_site;
        this.url_cover         = url_cover;
        this.update_datetime   = OffsetDateTime.now();
    }

    /**
     * Map -> AnimeInfo
     */
    public AnimeInfo(Map<String, String> data) {

        // 参数检查
        if(data == null || data.isEmpty()) {
            throw new IllegalArgumentException("AnimeInfo构造函数: 传入的Map<String, String>为null或空");
        }

        // 解析 ANI_ID
        {
            Integer parsedAniId = null;
            var ani_id_str = data.getOrDefault("ANI_ID", null);

            if(ani_id_str == null) {
                throw new IllegalArgumentException("AnimeInfo构造函数: Map<String, String>缺少必需的键 'ANI_ID'");
            }

            try {
                parsedAniId = Integer.parseInt(ani_id_str);
            } catch(NumberFormatException e) {
                throw new IllegalArgumentException("AnimeInfo构造函数: 键 'ANI_ID' 的值无法解析为整数: " + ani_id_str, e);
            }

            ANI_ID = parsedAniId;
        }

        // 解析 air_date
        {
            Date parsed_date = null;
            if(data.containsKey("air_date")) {
                var dateStr = data.get("air_date");
                if(dateStr != null) try {
                    var sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
                    parsed_date = sdf.parse(dateStr);
                } catch(ParseException _) {}
            }
            this.air_date = parsed_date;
        }

        title       = data.getOrDefault("title", null);
        title_cn    = data.getOrDefault("title_cn", null);
        aliases     = data.getOrDefault("aliases", null);
        description = data.getOrDefault("description", null);

        // 解析 episode_count
        {
            Integer parsedEpisodeCount = null;
            if(data.containsKey("episode_count")) {
                var episodeCountStr = data.get("episode_count");
                if(episodeCountStr != null) try {
                    parsedEpisodeCount = Integer.parseInt(episodeCountStr);
                } catch(NumberFormatException _) {}
            }
            episode_count = parsedEpisodeCount;
        }

        url_official_site = data.getOrDefault("url_official_site", null);
        url_cover         = data.getOrDefault("url_cover", null);
        update_datetime   = OffsetDateTime.now();
    }

    /**
     * TableData -> List<AnimeInfo>
     */
    public static List<AnimeInfo> convertAnimeInfo(TableData tableData) {

        // var aniIdIndex = tableData.GetColumnIndex("ANI_ID");
        // if(aniIdIndex == -1) return null;

        // var rows     = tableData.GetData();
        // var infoList = new ArrayList<AnimeInfo>();
        // for(var row : rows) {
        //     var info = new AnimeInfo(Integer.parseInt(row[aniIdIndex]));
        //     infoList.add(info);
        // }
        // return infoList;
        return null;
    }

    @Override
    public String toString() {
        return 
        "AnimeInfo{"
        + "ANI_ID=" + ANI_ID
        + ", air_date=" + UtilityFunctions.getDateString(air_date)
        + ", title='" + title + '\''
        + ", title_cn='" + title_cn + '\''
        + ", aliases='" + aliases + '\''
        + ", description='" + (description == null ? "null" : description.replace("\r\n", "\\n")) + '\''
        + ", episode_count=" + episode_count
        + ", url_official_site='" + url_official_site + '\''
        + ", url_cover='" + url_cover + '\''
        + ", update_datetime=" + UtilityFunctions.getDateString(update_datetime)
        + '}';
    }
}
