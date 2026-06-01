package NetAccess;

import com.apptasticsoftware.rssreader.Item;
import com.apptasticsoftware.rssreader.RssReader;

import Database.TorrentPageInfo;
import Util.Util;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.util.*;

class RSSParser {
    /**
     * 通过 RSS 链接获取种子信息列表
     * <p>
     * 需要提供 RSS 链接和番剧 ID
     * <p>
     * 返回值：种子信息列表，如果获取失败或没有种子则抛出异常
     */
    static Set<TorrentPageInfo> parseMikanRSS(RssReader reader, String rss_url) throws IOException {

        // 读取一个 RSS 链接
        var items = reader.read(rss_url).toList();

        // 遍历 RSS 条目，提取种子信息
        Set<TorrentPageInfo> res = new HashSet<>();
        for (var item : items) {
            // 如果 enclosure 不存在，就抛异常
            var enclosure = item.getEnclosure().orElseThrow(() -> new RuntimeException("RSS 条目缺少附件 enclosure"));

            // 填充信息
            var tor_hash       = enclosure.getUrl().substring(enclosure.getUrl().lastIndexOf("/") + 1).replace(".torrent", "");
            var air_datetime_str = item.getPubDate().orElse(null);
            OffsetDateTime air_datetime = null;
            if (air_datetime_str != null) {
                // 2026-05-31T23:01:07.56 -> OffsetDateTime.parse("2026-05-31T23:01:07.56+00:00")
                // var rssFormat      = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                // var dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

                // 解析字符串为 Date 转成标准格式字符串
                var air_datetime_std_str = air_datetime_str + "+08:00";
                air_datetime = OffsetDateTime.parse(air_datetime_std_str);
            }
            var url_download   = enclosure.getUrl();
            var url_page       = item.getLink().orElse(null);
            var title          = item.getTitle().orElse(null);
            var subtitle_group = ParseSubtitleGroup(title);
            var description    = item.getDescription().orElse(null);

            var record = new TorrentPageInfo(
                rss_url,
                tor_hash,
                air_datetime,
                url_download,
                url_page,
                title,
                subtitle_group,
                description
            );
            res.add(record);
        }
        return res;
    }

    /**
     * 通过 RSS 链接获取种子信息列表
     * <p>
     * 需要提供 RSS 链接和番剧 ID
     * <p>
     * 返回值：种子信息列表，如果获取失败或没有种子则返回 null
     */
    static Set<TorrentPageInfo>
    parseNyaaRSS(RssReader reader, String rss_url)
    throws IOException
    {
        // 自建一个 Map 来保存每个 Item 对应的扩展字段
        Map<Item, Map<String, String>> itemExtensions = new IdentityHashMap<>();
        reader.addItemExtension(
            "nyaa:infoHash",
            (item, value) -> itemExtensions.computeIfAbsent(item, _ -> new HashMap<>()).put("infoHash", value.trim())
        );

        // 读取一个 RSS 链接
        var items = reader.read(rss_url).toList();

        // 遍历 RSS 条目，提取种子信息
        Set<TorrentPageInfo> res = new HashSet<>();
        for (var item : items) {

            // 填充信息
            var url_rss          = rss_url;
            var ext              = itemExtensions.getOrDefault(item, Collections.emptyMap());
            var tor_hash         = ext.getOrDefault("infoHash", null);
            var air_datetime_str = item.getPubDate().orElse(null);
            var air_datetime     = Util.parseOffsetDateTime(air_datetime_str);

            var url_download     = item.getLink().orElse(null);
            var url_page         = item.getGuid().orElse(null);

            var title = item.getTitle().orElse(null);

            var subtitle_group = ParseSubtitleGroup(title);
            var description    = item.getDescription().orElse(null);

            var record = new TorrentPageInfo(
                url_rss,
                tor_hash,
                air_datetime,
                url_download,
                url_page,
                title,
                subtitle_group,
                description
            );
            res.add(record);
        }
        return res;
    }

    // 解析字幕组
    private static String ParseSubtitleGroup(String title) {
        String[] left_brackets = { "[", "【", "(", "（" };
        String[] right_brackets = { "]", "】", ")", "）" };

        for (int i = 0; i < left_brackets.length; i++) {
            // 如果匹配到左括号
            if (title.startsWith(left_brackets[i])) {
                int end_index = title.indexOf(right_brackets[i]); // 找到对应的右括号
                if (end_index != -1)
                    return title.substring(1, end_index); // 如果找到了右括号，就提取中间的字幕组名称
            }
        }
        return null; // 如果没有匹配到任何括号，就返回 null
    }

    private static Long ParseSizeToBytes(String size_str) {
        if (size_str == null || size_str.isEmpty())
            return null;

        size_str = size_str.trim().replace(",", ".");
        String[] parts = size_str.split(" ");
        if (parts.length != 2)
            return null;

        double value = Double.parseDouble(parts[0]);
        String unit = parts[1].toUpperCase();
        return switch (unit) {
            case "B" -> (long) value;
            case "KIB" -> (long) (value * 1024);
            case "MIB" -> (long) (value * 1024 * 1024);
            case "GIB" -> (long) (value * 1024 * 1024 * 1024);
            case "TIB" -> (long) (value * 1024L * 1024 * 1024 * 1024);

            // 万一 站点返回 MB / GB 等十进制单位，也兼容：
            case "KB" -> (long) (value * 1000);
            case "MB" -> (long) (value * 1000 * 1000);
            case "GB" -> (long) (value * 1000 * 1000 * 1000);
            case "TB" -> (long) (value * 1000L * 1000 * 1000 * 1000);

            default -> null;
        };
    }

}
