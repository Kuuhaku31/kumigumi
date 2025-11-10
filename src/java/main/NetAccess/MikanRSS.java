// MikanRSS.java

package NetAccess;

import com.apptasticsoftware.rssreader.Enclosure;
import com.apptasticsoftware.rssreader.Item;
import com.apptasticsoftware.rssreader.RssReader;
import utils.TableData;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import static NetAccess.Util.ParseSubtitleGroup;

public
class MikanRSS
{
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

        // 读取一个 RSS 链接
        List<Item> items = reader.read(rss_url).toList();

        // 遍历 RSS 条目，提取种子信息
        for(Item item : items)
        {
            var recode = data.new Record();

            // 如果 enclosure 不存在，就抛异常
            Enclosure enclosure = item.getEnclosure().orElseThrow(() -> new RuntimeException("RSS 条目缺少附件 enclosure"));

            // 填充信息
            var TOR_URL = enclosure.getUrl();
            recode.Set("TOR_URL", TOR_URL);

            var ANI_ID = String.valueOf(ani_id);
            recode.Set("ANI_ID", ANI_ID);

            var air_datetime = item.getPubDate().orElse("");
            recode.Set("air_datetime", air_datetime);

            var size = String.valueOf(enclosure.getLength().orElse(0L));
            recode.Set("size", size);

            var url_page = item.getLink().orElse("");
            recode.Set("url_page", url_page);

            var title = item.getTitle().orElse("");
            recode.Set("title", title);

            var subtitle_group = ParseSubtitleGroup(title);
            recode.Set("subtitle_group", subtitle_group);

            var description = item.getDescription().orElse("");
            recode.Set("description", description);
        }
    }


    void main(String[] args) throws IOException
    {
        IO.println(Arrays.toString(args));

        String rss_url = "https://mikanani.me/RSS/Bangumi?bangumiId=3698";

        HttpClient client = HttpClient.newBuilder()
            .proxy(ProxySelector.of(new InetSocketAddress("127.0.0.1", 10809))) // 例：Clash 代理
            .connectTimeout(Duration.ofSeconds(30))
            .build();
        RssReader reader = new RssReader(client);

        // 示例：读取一个 RSS 链接
        List<Item> items = reader.read(rss_url).toList();

        for(Item item : items)
        {
            System.out.println("标题: " + item.getTitle().orElse(""));
            System.out.println("链接: " + item.getLink().orElse(""));
            System.out.println("描述: " + item.getDescription().orElse(""));
            System.out.println("GUID: " + item.getGuid().orElse(""));
            System.out.println("发布日期: " + item.getPubDate().orElse(null));

            // enclosure 是可选字段，用 Optional 包装
            // 包装类（wrapper class），用来安全地表示“可能存在也可能不存在”的值
            item.getEnclosure().ifPresent(enclosure ->
            {
                // 如果存在 enclosure，则打印其信息
                System.out.println("附件链接: " + enclosure.getUrl());
                System.out.println("附件类型: " + enclosure.getType());
                System.out.println("附件大小: " + enclosure.getLength().orElse(0L));
            });

            System.out.println("——————————————");
        }

    }

}
