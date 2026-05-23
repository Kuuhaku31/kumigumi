package Database;

import java.sql.*;
import java.time.OffsetDateTime;
import java.util.Map;


public class TorrentPageInfo {

    public final String         URL_RSS;
    public final String         TOR_HASH;

    public final String         air_datetime;
    public final String         url_download;
    public final String         url_page;
    public final String         title;
    public final String         subtitle_group;
    public final String         description;

    public final OffsetDateTime update_datetime;


    static PreparedStatement GetUpsertStatement(Connection conn) throws SQLException {
        String upsertSqlFetch =
        """
        INSERT INTO torrent_page (
            URL_RSS,
            TOR_HASH,
            air_datetime,
            url_download,
            url_page,
            title,
            subtitle_group,
            description,
            update_datetime
        )
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        ON CONFLICT(URL_RSS, TOR_HASH) DO UPDATE SET
            air_datetime      = excluded.air_datetime,
            url_download      = excluded.url_download,
            url_page          = excluded.url_page,
            title             = excluded.title,
            subtitle_group    = excluded.subtitle_group,
            description       = excluded.description,
            update_datetime   = excluded.update_datetime;
        """;
        return conn.prepareStatement(upsertSqlFetch);
    }

    void SetParams(PreparedStatement ps) throws SQLException {
        Utils.safeSetString         (ps,  1, URL_RSS        );
        Utils.safeSetString         (ps,  2, TOR_HASH       );
        Utils.safeSetString         (ps,  3, air_datetime   );
        Utils.safeSetString         (ps,  4, url_download   );
        Utils.safeSetString         (ps,  5, url_page       );
        Utils.safeSetString         (ps,  6, title          );
        Utils.safeSetString         (ps,  7, subtitle_group );
        Utils.safeSetString         (ps,  8, description    );
        Utils.safeSetOffsetDateTime (ps,  9, update_datetime);
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

        air_datetime    = data.getOrDefault("air_datetime", null);
        url_download    = data.getOrDefault("url_download", null);
        url_page        = data.getOrDefault("url_page", null);
        title           = data.getOrDefault("title", null);
        subtitle_group  = data.getOrDefault("subtitle_group", null);
        description     = data.getOrDefault("description", null);

        update_datetime = OffsetDateTime.now();
    }
}
