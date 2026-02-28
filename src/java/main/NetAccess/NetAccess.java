package NetAccess;

import com.apptasticsoftware.rssreader.RssReader;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static NetAccess.BangumiParser.ParseAnimeInfo;
import static NetAccess.BangumiParser.ParseEpisodeInfo;
import static NetAccess.RSSParser.parseMikanRSS;
import static NetAccess.RSSParser.parseNyaaRSS;

public class NetAccess {
    // 复用 HttpClient 实例
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();

    public static byte[] DownloadFile(String url_str) throws URISyntaxException, IOException {
        var url = new URI(url_str).toURL(); // 创建URL对象
        var conn = (HttpURLConnection) url.openConnection(); // 打开连接
        conn.setRequestMethod("GET"); // 设置请求方法
        conn.setRequestProperty("User-Agent", "Mozilla/5.0"); // 添加 User-Agent

        // 直接读取原始字节流（适用于二进制文件如 torrent）
        try (var in = conn.getInputStream()) {
            return in.readAllBytes();
        }
    }

    public static Map<String, String> FetchAnimeInfo(int anime_id) throws URISyntaxException, IOException {
        // 解析 anime 信息
        return ParseAnimeInfo(GetInfo(QueryType.anime_info, anime_id));
    }

    public static List<Map<String, String>> FetchEpisodeInfo(int anime_id) throws URISyntaxException, IOException {
        // 解析 episode 信息
        var ep_list = GetInfo(QueryType.episode_list, anime_id).getJSONArray("data");

        List<Map<String, String>> res = new ArrayList<>();
        for (var item : ep_list)
            res.add(ParseEpisodeInfo((JSONObject) item));

        return res;
    }

    public static List<Map<String, String>> FetchAnimeTorrentInfo(String rss_url) throws IOException {
        var reader = new RssReader(httpClient);
        return switch (detectRSSSourceType(rss_url)) {
            case MIKAN -> parseMikanRSS(reader, rss_url);
            case NYAA -> parseNyaaRSS(reader, rss_url);
            case UNKNOWN -> throw new IOException("不支持的RSS源: " + rss_url);
        };
    }

    public static List<Map<String, String>> FetchAnimeTorrentInfo(String rss_url, int anime_id) throws IOException {
        return FetchAnimeTorrentInfo(rss_url).stream()
                .peek(tor -> tor.put("ANI_ID", String.valueOf(anime_id)))
                .toList();
    }

    private static JSONObject GetInfo(QueryType type, int anime_id) throws URISyntaxException, IOException {
        var bangumi_server = "https://api.bgm.tv";

        String format_str;
        switch (type) {
            case anime_info -> format_str = "%s/v0/subjects/%d";
            case episode_list -> format_str = "%s/v0/episodes?subject_id=%d";
            default -> throw new UnsupportedOperationException("BangumiAPI GetInfo: 未知的查询类型");
        }
        String url_str = String.format(format_str, bangumi_server, anime_id);
        String res_str = Get(url_str);

        return new JSONObject(res_str);
    }

    private static String Get(String url_str) throws URISyntaxException, IOException {
        var url = new URI(url_str).toURL(); // 创建URL对象
        var conn = (HttpURLConnection) url.openConnection(); // 打开连接
        conn.setRequestMethod("GET"); // 设置请求方法
        conn.setRequestProperty("User-Agent", "Mozilla/5.0"); // 添加 User-Agent

        // 读取响应
        var in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        var response = new StringBuilder();

        // 逐行读取响应内容
        String input_line;
        while ((input_line = in.readLine()) != null) response.append(input_line);

        in.close();

        return response.toString();
    }

    /**
     * 检测 RSS 源类型
     */
    private static RSSSourceType detectRSSSourceType(String rss_url) {
        if (rss_url.startsWith("https://mikanani.me") || rss_url.startsWith("https://mikan.tangbai.cc")) {
            return RSSSourceType.MIKAN;
        } else if (rss_url.startsWith("https://nyaa")) {
            return RSSSourceType.NYAA;
        }
        return RSSSourceType.UNKNOWN;
    }

    // RSS 源类型
    private enum RSSSourceType {
        MIKAN, NYAA, UNKNOWN
    }

    // 请求类型
    private enum QueryType {
        anime_info,
        episode_list,
    }
}
