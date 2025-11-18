// NetAccess.NyaaRSS.java


package NetAccess;

import com.apptasticsoftware.rssreader.Item;
import com.apptasticsoftware.rssreader.RssReader;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.http.HttpClient;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;

import static NetAccess.Util.ParseSizeToBytes;
import static NetAccess.Util.ParseSubtitleGroup;

public
class NyaaRSS
{
    private static final SimpleDateFormat rssFormat      = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", java.util.Locale.ENGLISH);
    private static final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 通过 RSS 链接获取种子信息列表
     * <p>
     * 需要提供 RSS 链接和番剧 ID
     * <p>
     * 返回值：种子信息列表，如果获取失败或没有种子则返回 null
     */
    public static
    List<Map<String, String>> GetTorrentData(String rss_url, int ani_id) throws IOException
    {
        HttpClient client = HttpClient.newBuilder()
            .proxy(ProxySelector.of(new InetSocketAddress("127.0.0.1", 10809))) // 例：Clash 代理
            .connectTimeout(Duration.ofSeconds(30))
            .build();
        RssReader reader = new RssReader(client);

        // 自建一个 Map 来保存每个 Item 对应的扩展字段
        Map<Item, Map<String, String>> itemExtensions = new IdentityHashMap<>();
        reader.addItemExtension("nyaa:size", (item, value) ->
            itemExtensions.computeIfAbsent(item, _ -> new HashMap<>()).put("size", value.trim())
        );

        // 读取一个 RSS 链接
        List<Item> items = reader.read(rss_url).toList();

        // 遍历 RSS 条目，提取种子信息
        List<Map<String, String>> res = new ArrayList<>();
        for(var item : items)
        {
            // 填充信息
            Map<String, String> recode = new HashMap<>();
            res.add(recode);

            // 填充信息
            recode.put("TOR_URL", item.getLink().orElse(null));
            recode.put("ANI_ID", String.valueOf(ani_id));

            try
            {
                // 解析字符串为 Date 转成标准格式字符串
                var air_datetime = dateTimeFormat.format(rssFormat.parse(item.getPubDate().orElse(null)));
                recode.put("air_datetime", air_datetime);
            }
            catch(ParseException _) { recode.put("air_datetime", null); }

            var ext  = itemExtensions.getOrDefault(item, Collections.emptyMap());
            var len  = ParseSizeToBytes(ext.getOrDefault("size", null));
            var size = len == null ? null : String.valueOf(len);
            recode.put("size", size);

            recode.put("url_page", item.getGuid().orElse(null));

            var title = item.getTitle().orElse(null);
            recode.put("title", title);
            recode.put("subtitle_group", ParseSubtitleGroup(title));

            recode.put("description", item.getDescription().orElse(null));
        }
        return res;
    }

}
