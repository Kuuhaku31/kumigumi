package Database;

import java.sql.*;
import java.util.Map;


public class RSSInfo {

    public final String  URL_RSS;
    public final Integer ANI_ID;


    static PreparedStatement GetUpsertStatement(Connection conn) throws SQLException {
        String upsertSqlFetch =
        """
        INSERT INTO rss (
            URL_RSS,
            ANI_ID
        )
        VALUES (?, ?)
        ON CONFLICT(URL_RSS) DO UPDATE SET
            ANI_ID = excluded.ANI_ID;
        """;
        return conn.prepareStatement(upsertSqlFetch);
    }

    void SetParams(PreparedStatement ps) throws SQLException {
        Utils.safeSetString(ps, 1, URL_RSS);
        Utils.safeSetInt(ps, 2, ANI_ID);
    }


    public RSSInfo(Map<String, String> data) {

        // 参数检查
        if(data == null || data.isEmpty()) {
            throw new IllegalArgumentException("RSSInfo构造函数: 传入的Map<String, String>为null或空");
        }

        {
            URL_RSS = data.getOrDefault("URL_RSS", null);
            if(URL_RSS == null) throw new IllegalArgumentException("RSSInfo构造函数: URL_RSS不能为空");
        }

        {
            Integer aniId = null;
            var aniIdStr = data.getOrDefault("ANI_ID", null);
            if(aniIdStr != null) {
                try { aniId = Integer.parseInt(aniIdStr); }
                catch(NumberFormatException _) {}
            }
            ANI_ID = aniId;
        }
    }
}
