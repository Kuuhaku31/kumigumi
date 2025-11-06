// NyaaRSS.java


import com.apptasticsoftware.rssreader.Item;
import com.apptasticsoftware.rssreader.RssReader;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;

public
class NyaaRSS
{
    /**
     * 通过 RSS 链接获取种子信息列表
     * <p>
     * 需要提供 RSS 链接和番剧 ID
     * <p>
     * 返回值：种子信息列表，如果获取失败或没有种子则返回 null
     */
    public static
    String[][] GetTorrentData(String rss_url, int ani_id) throws IOException
    {
        if(rss_url == null) return new String[0][];

        HttpClient client = HttpClient.newBuilder()
            .proxy(ProxySelector.of(new InetSocketAddress("127.0.0.1", 10809))) // 例：Clash 代理
            .connectTimeout(Duration.ofSeconds(30))
            .build();
        RssReader reader = new RssReader(client);

        // 读取一个 RSS 链接
        List<Item> items = reader.read(rss_url).toList();

        // 遍历 RSS 条目，提取种子信息
        String[][] torrent_info_list = new String[items.size()][0];

        int i = 0;
        for(Item item : items)
        {
            // 如果 enclosure 不存在，就抛异常
            // Enclosure enclosure = item.getEnclosure().orElseThrow(() -> new RuntimeException("RSS 条目缺少附件 enclosure"));

            // 填充信息
            // String TOR_URL        = enclosure.getUrl();
            String TOR_URL      = item.getLink().orElse(null);
            String ANI_ID       = String.valueOf(ani_id);
            String air_datetime = item.getPubDate().orElse(null);
            // String size         = String.valueOf(enclosure.getLength().orElse(0L));
            String size           = null;
            String url_page       = item.getGuid().orElse(null);
            String title          = item.getTitle().orElse(null);
            String subtitle_group = parse_subtitle_group(title);
            String description    = item.getDescription().orElse(null);

            // 一条种子信息
            String[] torrent_info = new String[] {TOR_URL, ANI_ID, air_datetime, size, url_page, title, subtitle_group, description};

            torrent_info_list[i++] = (torrent_info);
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

    static
    void main() throws IOException
    {
        String rss_url = "https://nyaa.si/?page=rss&q=Taiyou+yori+mo+Mabushii+Hoshi&c=0_0&f=0";

        var data = GetTorrentData(rss_url, 1);

        // for(String[] datum : data)
        // {
        //     System.out.println(Arrays.toString(datum));
        // }
        for(var t : data[0])
        {
            System.out.println(t);
        }
    }
}
