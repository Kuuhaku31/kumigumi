// NetAccess.NyaaRSS.java


package NetAccess;

import com.apptasticsoftware.rssreader.Item;
import com.apptasticsoftware.rssreader.RssReader;
import utils.TableData;

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
    private static final SimpleDateFormat rssFormat   = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", java.util.Locale.ENGLISH);
    private static final SimpleDateFormat mysqlFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 通过 RSS 链接获取种子信息列表
     * <p>
     * 需要提供 RSS 链接和番剧 ID
     * <p>
     * 返回值：种子信息列表，如果获取失败或没有种子则返回 null
     */
    public static
    void GetTorrentData(TableData data, String rss_url, int ani_id) throws IOException
    {
        if(rss_url == null) return;

        HttpClient client = HttpClient.newBuilder()
            .proxy(ProxySelector.of(new InetSocketAddress("127.0.0.1", 10809))) // 例：Clash 代理
            .connectTimeout(Duration.ofSeconds(30))
            .build();
        RssReader reader = new RssReader(client);

        // 自建一个 Map 来保存每个 Item 对应的扩展字段
        Map<Item, Map<String, String>> itemExtensions = new IdentityHashMap<>();
        reader.addItemExtension("nyaa:size", (item, value) ->
            itemExtensions.computeIfAbsent(item, k -> new HashMap<>()).put("size", value.trim())
        );

        // 读取一个 RSS 链接
        List<Item> items = reader.read(rss_url).toList();

        // 遍历 RSS 条目，提取种子信息
        for(Item item : items)
        {
            var recode = data.new Record();


            // 填充信息
            recode.Set("TOR_URL", item.getLink().orElse(null));
            recode.Set("ANI_ID", String.valueOf(ani_id));

            // 1. 解析字符串为 Date
            // 2. 转成 MySQL 标准格式字符串
            String air_datetime = null;
            try { air_datetime = mysqlFormat.format(rssFormat.parse(item.getPubDate().orElse(null))); }
            catch(ParseException _) { }
            recode.Set("air_datetime", air_datetime);

            var ext  = itemExtensions.getOrDefault(item, Collections.emptyMap());
            var len  = ParseSizeToBytes(ext.getOrDefault("size", null));
            var size = len == null ? null : String.valueOf(len);
            recode.Set("size", size);

            var url_page = item.getGuid().orElse(null);
            recode.Set("url_page", url_page);

            var title = item.getTitle().orElse(null);
            recode.Set("title", title);

            var subtitle_group = ParseSubtitleGroup(title);
            recode.Set("subtitle_group", subtitle_group);

            var description = item.getDescription().orElse(null);
            recode.Set("description", description);
        }
    }

}
