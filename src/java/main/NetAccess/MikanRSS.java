// MikanRSS.java

package NetAccess;

import com.apptasticsoftware.rssreader.Item;
import com.apptasticsoftware.rssreader.RssReader;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;

public
class MikanRSS
{
    static
    void main(String[] args) throws IOException
    {
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
