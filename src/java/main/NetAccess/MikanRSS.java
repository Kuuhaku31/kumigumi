// MikanRSS.java

package NetAccess;

import com.apptasticsoftware.rssreader.Enclosure;
import com.apptasticsoftware.rssreader.Item;
import com.apptasticsoftware.rssreader.RssReader;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public
class MikanRSS
{
    static
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


    // 通过 RSS 链接获取种子信息列表
    // 需要提供 RSS 链接和番剧 ID
    // 返回值：种子信息列表，如果获取失败或没有种子则返回 null
    public static
    ArrayList<ArrayList<String>> GetTorrentInfoList(String rss_url, int ani_id) throws IOException
    {
        HttpClient client = HttpClient.newBuilder()
            .proxy(ProxySelector.of(new InetSocketAddress("127.0.0.1", 10809))) // 例：Clash 代理
            .connectTimeout(Duration.ofSeconds(30))
            .build();
        RssReader reader = new RssReader(client);

        // 读取一个 RSS 链接
        List<Item> items = reader.read(rss_url).toList();

        // 遍历 RSS 条目，提取种子信息
        ArrayList<ArrayList<String>> torrent_info_list = new ArrayList<>();
        for(Item item : items)
        {
            // 如果 enclosure 不存在，就抛异常
            Enclosure enclosure = item.getEnclosure().orElseThrow(() -> new RuntimeException("RSS 条目缺少附件 enclosure"));

            // 填充信息
            String TOR_URL        = enclosure.getUrl();
            String ANI_ID         = String.valueOf(ani_id);
            String air_datetime   = item.getPubDate().orElse("");
            String size           = String.valueOf(enclosure.getLength().orElse(0L));
            String url_page       = item.getLink().orElse("");
            String title          = item.getTitle().orElse("");
            String subtitle_group = parse_subtitle_group(title);
            String description    = item.getDescription().orElse("");

            ArrayList<String> torrent_info = new ArrayList<>();
            Collections.addAll(
                torrent_info,
                TOR_URL,
                ANI_ID,
                air_datetime,
                size,
                url_page,
                title,
                subtitle_group,
                description
            );
            torrent_info_list.add(torrent_info);
        }

        return torrent_info_list;
    }


    // 解析字幕组
    private static
    String parse_subtitle_group(String title)
    {
        String[] left_brackets  = {"[", "【", "(", "{"};
        String[] right_brackets = {"]", "】", ")", "}"};

        for(int i = 0; i < left_brackets.length; i++)
        {
            // 如果匹配到左括号
            if(title.startsWith(left_brackets[i]))
            {
                // 找到对应的右括号
                int end_index = title.indexOf(right_brackets[i]);

                // 如果找到了右括号，就提取中间的字幕组名称
                if(end_index != -1) return title.substring(1, end_index);
            }
        }

        // 如果没有匹配到任何括号，就返回未知字幕组
        return "未知字幕组";
    }

}
