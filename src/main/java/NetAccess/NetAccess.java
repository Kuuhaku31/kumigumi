package NetAccess;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

import Database.Info.TorrentPageInfo;
import Database.Info.AnimeInfo;
import Database.Info.EpisodeInfo;

import com.apptasticsoftware.rssreader.RssReader;
import org.json.JSONObject;


public final class NetAccess {

    private static final String   USER_AGENT   = "Mozilla/5.0";          // 模拟浏览器的 User-Agent，避免被部分服务器拒绝访问
    private static final Duration HTTP_TIMEOUT = Duration.ofSeconds(30); // HTTP 请求超时时间，单位秒

    // RssReader 支持注入 HttpClient
    // 整个模块共享一个实例，避免重复创建连接资源
    private static final HttpClient
    httpClient = HttpClient.newBuilder().connectTimeout(HTTP_TIMEOUT).build();

    // 工具类，禁止实例化。
    private NetAccess() {}

    /**
     * 下载指定 URL 的内容，返回原始字节。
     * <p>
     * 该方法用于 torrent 等二进制资源，不做字符编码转换。
     */
    public static byte[]
    DownloadFile(String url_str)  throws URISyntaxException, IOException {

        var conn = open_get_connection(url_str);

        try(var in = conn.getInputStream()) { return in.readAllBytes(); }
        finally { conn.disconnect(); }
    }

    /**
     * 根据 Bangumi 番剧 ID 获取番剧信息。
     */
    public static AnimeInfo 
    FetchAnimeInfo(Integer anime_id) throws URISyntaxException, IOException {

        var info_json = fetch_bgm_json(BangumiQueryType.ANIME_INFO, anime_id);
        return BangumiParser.parseAnimeInfo(info_json);
    }

    /**
     * 根据 Bangumi 番剧 ID 获取分集信息集合。
     */
    public static Set<EpisodeInfo> 
    FetchEpisodeInfoSet(Integer anime_id) throws URISyntaxException, IOException {

        var info_json = fetch_bgm_json(BangumiQueryType.EPISODE_LIST, anime_id);
        if(info_json == null || !info_json.has("data")) return null;

        var ep_list = info_json.getJSONArray("data");
        if(ep_list == null) return null;

        Set<EpisodeInfo> res = new HashSet<>();
        for(var item : ep_list) res.add(BangumiParser.parseEpisodeInfo((JSONObject)item));

        return res.isEmpty() ? null : res;
    }

    /**
     * 根据 RSS URL 获取种子页信息集合。
     * <p>
     * 内部会根据 URL 前缀选择 Mikan 或 Nyaa 的 RSS 解析器。
     */
    public static Set<TorrentPageInfo>
    FetchTorrentPageInfoSet(String rss_url) throws IOException {

        return switch(RSSSourceType.detectRSSSourceType(rss_url)) {
            case MIKAN   -> parse_mikan_rss(rss_url);
            case NYAA    -> parse_nyaa_rss(rss_url);
            case UNKNOWN -> throw new IOException("不支持的RSS源: " + rss_url);
        };
    }


    // 以下是内部实现细节，外部调用不需要关心。

    private static JSONObject
    fetch_bgm_json(BangumiQueryType type, Integer anime_id) throws URISyntaxException, IOException {

        if(anime_id == null) throw new IllegalArgumentException("Bangumi 番剧 ID 不能为空");

        var url_str = type.formatUrl(anime_id);

        var conn = open_get_connection(url_str); // 打开连接，设置请求参数
        try(var in     = conn.getInputStream();
            var reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {

            var    response = new StringBuilder();
            String input_line;
            while((input_line = reader.readLine()) != null) response.append(input_line);
            return new JSONObject(response.toString());

        } finally {
            conn.disconnect();
        }
    }

    /**
     * 解析 Mikan RSS。
     * <p>
     * Mikan 的下载地址在 enclosure 中，种子 hash 从 enclosure URL 末尾提取。
     */
    private static Set<TorrentPageInfo>
    parse_mikan_rss(String rss_url) throws IOException {

        var reader = new RssReader(httpClient);
        var items  = reader.read(rss_url).toList();

        Set<TorrentPageInfo> res = new HashSet<>();
        for(var item : items) res.add(RSSParser.parseMikanItem(rss_url, item));

        return res;
    }

     /**
     * 解析 Nyaa RSS。
     * <p>
     * Nyaa 的 infoHash 是扩展字段，需要在读取 RSS 前注册扩展解析器。
     */
    private static Set<TorrentPageInfo>
    parse_nyaa_rss(String rss_url) throws IOException {

        var reader          = new RssReader(httpClient);
        var item_extensions = RSSParser.registerNyaaExtensions(reader);

        var items = reader.read(rss_url).toList();

        Set<TorrentPageInfo> res = new HashSet<>();
        for(var item : items) res.add(RSSParser.parseNyaaItem(rss_url, item, item_extensions));

        return res;
    }

    /**
     * 打开一个 HTTP GET 连接，设置 User-Agent 和超时等参数。
     */
    private static HttpURLConnection
    open_get_connection(String url_str)
    throws URISyntaxException, MalformedURLException, IOException {

        if(url_str == null) throw new IllegalArgumentException("URL 不能为 null");

        var url  = new URI(url_str).toURL();                // 验证 URL 格式
        var conn = (HttpURLConnection)url.openConnection(); // 获取连接对象

        conn.setRequestMethod("GET");                         // 设置请求方法为 GET
        conn.setRequestProperty("User-Agent", USER_AGENT);    // 设置 User-Agent 模拟浏览器
        conn.setConnectTimeout((int)HTTP_TIMEOUT.toMillis()); // 设置连接超时时间
        conn.setReadTimeout((int)HTTP_TIMEOUT.toMillis());    // 设置读取超时时间

        return conn;
    }
}
