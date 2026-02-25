package NetAccess;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.http.HttpClient;
import java.time.Duration;

import com.apptasticsoftware.rssreader.RssReader;

public class TestNetAccess {
    public static void main(String[] args) throws IOException {
        System.out.println("NetAccess module test");

        // 参数准备
        final var rss_url = "https://nyaa.si/?page=rss&q=Tamon-kun+Ima+Docchi%21%3F&c=0_0&f=0";
        final var rss_url_mikan = "https://mikan.tangbai.cc/RSS/Bangumi?bangumiId=3862";
        System.out.println("Testing RSS URL: " + rss_url);
        System.out.println("Testing RSS URL (Mikan): " + rss_url_mikan);

        // 构建 client
        var client = HttpClient.newBuilder()
                .proxy(ProxySelector.of(new InetSocketAddress("127.0.0.1", 10809))) // 例：Clash 代理
                .connectTimeout(Duration.ofSeconds(30))
                .build();
        var reader = new RssReader(client);

        // 解析 RSS
        var res = RSSParser.parseNyaaRSS(reader, rss_url);
        var res_mikan = RSSParser.parseMikanRSS(reader, rss_url_mikan);

        // 输出结果
        for (var item : res) {
            System.out.println("---- Item ----");
            for (var entry : item.entrySet()) {
                System.out.println(entry.getKey() + ": " + entry.getValue());
            }
        }

        System.out.println("---- Mikan Items ----");
        for (var item : res_mikan) {
            System.out.println("---- Item ----");
            for (var entry : item.entrySet()) {
                System.out.println(entry.getKey() + ": " + entry.getValue());
            }
        }
    }
}
