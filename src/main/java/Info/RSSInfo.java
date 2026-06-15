package Info;

import java.util.Map;
import java.util.Set;

import Utils.DataBlock;


public class RSSInfo extends BaseInfo {

    public final String  URL_RSS;
    public final Integer ANI_ID;

    public RSSInfo(String url_rss, Integer ani_id) {
        if(url_rss == null) {
            throw new IllegalArgumentException("RSSInfo构造函数: URL_RSS不能为空");
        }
        URL_RSS = url_rss;
        ANI_ID = ani_id;
    }

    /**
     * 从Map<String, String>创建RSSInfo实例
     * @param data
     */
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

    /**
     * 从 DataBlock（数据块）创建 RSSInfo 实例
     * @param data
     */
    public static Set<RSSInfo> ParseRSSInfoByDataBlock(DataBlock data) {

        // 获取列索引
        var url_rss_index = data.GetColumnIndex("URL_RSS");
        var ani_id_index  = data.GetColumnIndex("ANI_ID");

        // 构造RSSInfo对象并添加到集合
        Set<RSSInfo> infoSet = new java.util.HashSet<>();
        for (var rowIndex = 0; rowIndex < data.GetRowSize(); rowIndex++) {

            // 解析 URL_RSS 和 ANI_ID
            String url_rss = null;
            Integer ani_id = null;
            var     row = data.GetRow(rowIndex);

            if(url_rss_index != -1) url_rss = row[url_rss_index];
            if(ani_id_index != -1) {
                try {
                    ani_id = Integer.parseInt(row[ani_id_index]);
                } catch(NumberFormatException _) {}
            }

            try {
                var new_info = new RSSInfo(url_rss, ani_id);
                infoSet.add(new_info);
            } catch(IllegalArgumentException _) {
                // 忽略无效的RSSInfo对象
            }
        }
        return infoSet;
    }


    @Override
    public String toPrintString(String indent, boolean enable_color) {
        return formatInfo("RSSInfo", indent, enable_color, new Object[][] {
            { "URL_RSS", URL_RSS },
            { "ANI_ID", ANI_ID }
        });
    }

    @Override
    public String toString() {
        return toPrintString("", false);
    }
}
