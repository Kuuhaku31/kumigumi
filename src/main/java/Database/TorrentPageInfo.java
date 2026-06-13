package Database;

import java.sql.*;
import java.time.OffsetDateTime;
import java.util.Map;


public class TorrentPageInfo extends Info {

    public final String         URL_RSS;
    public final String         TOR_HASH;

    public final OffsetDateTime air_datetime;
    public final String         url_download;
    public final String         url_page;
    public final String         title;
    public final String         subtitle_group;
    public final String         description;

    public final OffsetDateTime update_datetime;

    @Override
    void setParams(PreparedStatement ps) throws SQLException {
        Utils.safeSetString         (ps,  1, URL_RSS        );
        Utils.safeSetString         (ps,  2, TOR_HASH       );
        Utils.safeSetOffsetDateTime (ps,  3, air_datetime   );
        Utils.safeSetString         (ps,  4, url_download   );
        Utils.safeSetString         (ps,  5, url_page       );
        Utils.safeSetString         (ps,  6, title          );
        Utils.safeSetString         (ps,  7, subtitle_group );
        Utils.safeSetString         (ps,  8, description    );
        Utils.safeSetOffsetDateTime (ps,  9, update_datetime);
    }


    public TorrentPageInfo(
        String         URL_RSS,
        String         TOR_HASH,
        OffsetDateTime air_datetime,
        String         url_download,
        String         url_page,
        String         title,
        String         subtitle_group,
        String         description

    ) {

        // 参数检查
        if(URL_RSS == null || TOR_HASH == null) {
            throw new IllegalArgumentException("TorrentPageInfo构造函数: URL_RSS和TOR_HASH不能为空");
        }

        this.URL_RSS         = URL_RSS;
        this.TOR_HASH        = TOR_HASH;
        this.air_datetime    = air_datetime;
        this.url_download    = url_download;
        this.url_page        = url_page;
        this.title           = title;
        this.subtitle_group  = subtitle_group;
        this.description     = description;
        this.update_datetime = OffsetDateTime.now();
    }

    /**
     * Map -> TorrentPageInfo
     */
    public TorrentPageInfo(Map<String, String> data) {

        // 参数检查
        if(data == null || data.isEmpty()) {
            throw new IllegalArgumentException("TorrentPageInfo构造函数: 传入的Map<String, String>为null或空");
        }

        {
            URL_RSS = data.getOrDefault("URL_RSS", null);
            if(URL_RSS == null) throw new IllegalArgumentException("TorrentPageInfo构造函数: URL_RSS不能为空");
        }


        {
            TOR_HASH = data.getOrDefault("TOR_HASH", null);
            if(TOR_HASH == null) throw new IllegalArgumentException("TorrentPageInfo构造函数: TOR_HASH不能为空");
        }

        { 
            var air_datetime_str = data.getOrDefault("air_datetime", null); 
            if(air_datetime_str != null) {
                try {
                    air_datetime = OffsetDateTime.parse(air_datetime_str);
                } catch(Exception e) {
                    throw new IllegalArgumentException("TorrentPageInfo构造函数: air_datetime字段无法解析为OffsetDateTime: " + air_datetime_str, e);
                }
            } else {
                air_datetime = null;
            }
        }
        url_download    = data.getOrDefault("url_download", null);
        url_page        = data.getOrDefault("url_page", null);
        title           = data.getOrDefault("title", null);
        subtitle_group  = data.getOrDefault("subtitle_group", null);
        description     = data.getOrDefault("description", null);

        update_datetime = OffsetDateTime.now();
    }

    @Override
    public String toString() {
        var format_str = "TorrentPageInfo { URL_RSS: %s, TOR_HASH: %s, air_datetime: %s, url_download: %s, url_page: %s, title: %s, subtitle_group: %s, description: %s }";
        return String.format(format_str,
            URL_RSS,
            TOR_HASH,
            air_datetime,
            url_download,
            url_page,
            title,
            subtitle_group,
            description
        );
    }

    public String toFormatString() {
        var format_str =
        """
        URL_RSS:        %s
        TOR_HASH:       %s
        air_datetime:   %s
        url_download:   %s
        url_page:       %s
        title:          %s
        subtitle_group: %s
        description:

        %s
        """;
        return String.format(format_str,
            URL_RSS,
            TOR_HASH,
            air_datetime,
            url_download,
            url_page,
            title,
            subtitle_group,
            description
        );
    }
}
