// MikanRSS.java

package NetAccess;

import com.apptasticsoftware.rssreader.Item;
import com.apptasticsoftware.rssreader.RssReader;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static NetAccess.Util.ParseSubtitleGroup;

public
class MikanRSS
{
    /**
     * 通过 RSS 链接获取种子信息列表
     * <p>
     * 需要提供 RSS 链接和番剧 ID
     * <p>
     * 返回值：种子信息列表，如果获取失败或没有种子则抛出异常
     */
    public static
    List<Map<String, String>> GetTorrentData(String rss_url, int ani_id) throws IOException
    {
        HttpClient client = HttpClient.newBuilder()
            .proxy(ProxySelector.of(new InetSocketAddress("127.0.0.1", 10809))) // 例：Clash 代理
            .connectTimeout(Duration.ofSeconds(30))
            .build();
        RssReader reader = new RssReader(client);

        // 读取一个 RSS 链接
        List<Item> items = reader.read(rss_url).toList();

        // 遍历 RSS 条目，提取种子信息
        List<Map<String, String>> res = new ArrayList<>();
        for(var item : items)
        {
            // 如果 enclosure 不存在，就抛异常
            var enclosure = item.getEnclosure().orElseThrow(() -> new RuntimeException("RSS 条目缺少附件 enclosure"));

            // 填充信息
            Map<String, String> recode = new HashMap<>();
            res.add(recode);

            recode.put("TOR_URL", enclosure.getUrl());
            recode.put("ANI_ID", String.valueOf(ani_id));
            recode.put("air_datetime", item.getPubDate().orElse(null));
            recode.put("size", String.valueOf(enclosure.getLength().orElse(null)));
            recode.put("url_page", item.getLink().orElse(null));

            var title = item.getTitle().orElse(null);
            recode.put("title", title);
            recode.put("subtitle_group", ParseSubtitleGroup(title));
            recode.put("description", item.getDescription().orElse(null));
        }
        return res;
    }
}
