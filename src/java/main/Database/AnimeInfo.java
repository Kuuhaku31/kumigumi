package Database;

import java.sql.*;
import java.text.ParseException;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

import Util.TableData;


public class AnimeInfo {

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


    static PreparedStatement GetUpsertStatement(Connection conn) throws SQLException {
        String upsertSqlFetch =
        """
        INSERT INTO anime (
            ANI_ID,
            air_date,
            title,
            title_cn,
            aliases,
            description,
            episode_count,
            url_official_site,
            url_cover,
            update_datetime
        )
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        ON CONFLICT(ANI_ID) DO UPDATE SET
            air_date            = excluded.air_date,
            title               = excluded.title,
            title_cn            = excluded.title_cn,
            aliases             = excluded.aliases,
            description         = excluded.description,
            episode_count       = excluded.episode_count,
            url_official_site   = excluded.url_official_site,
            url_cover           = excluded.url_cover,
            update_datetime     = excluded.update_datetime;
        """;
        return conn.prepareStatement(upsertSqlFetch);
    }

    void SetParams(PreparedStatement ps) throws SQLException {
        Utils.safeSetInt            (ps,  1, ANI_ID           );
        Utils.safeSetDate           (ps,  2, air_date         );
        Utils.safeSetString         (ps,  3, title            );
        Utils.safeSetString         (ps,  4, title_cn         );
        Utils.safeSetString         (ps,  5, aliases          );
        Utils.safeSetString         (ps,  6, description      );
        Utils.safeSetInt            (ps,  7, episode_count    );
        Utils.safeSetString         (ps,  8, url_official_site);
        Utils.safeSetString         (ps,  9, url_cover        );
        Utils.safeSetOffsetDateTime (ps, 10, update_datetime  );
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

        update_datetime = OffsetDateTime.now(); // 设置更新时间为当前时间
    }

    /**
     * TableData -> List<AnimeInfo>
     */
    public static List<AnimeInfo> convertAnimeInfo(TableData tableData) {

        // var aniIdIndex = tableData.GetHeaderIndex("ANI_ID");
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
        + ", air_date=" + Util.Util.getDateString(air_date)
        + ", title='" + title + '\''
        + ", title_cn='" + title_cn + '\''
        + ", aliases='" + aliases + '\''
        + ", description='" + (description == null ? "null" : description.replace("\r\n", "\\n")) + '\''
        + ", episode_count=" + episode_count
        + ", url_official_site='" + url_official_site + '\''
        + ", url_cover='" + url_cover + '\'' + '}';
    }
}
