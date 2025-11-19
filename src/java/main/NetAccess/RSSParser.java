package NetAccess;


import com.apptasticsoftware.rssreader.Item;
import com.apptasticsoftware.rssreader.RssReader;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


class RSSParser
{
    /**
     * 通过 RSS 链接获取种子信息列表
     * <p>
     * 需要提供 RSS 链接和番剧 ID
     * <p>
     * 返回值：种子信息列表，如果获取失败或没有种子则抛出异常
     */
    static
    List<Map<String, String>> parseMikanRSS(RssReader reader, String rss_url) throws IOException
    {
        // 读取一个 RSS 链接
        var items = reader.read(rss_url).toList();

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

    /**
     * 通过 RSS 链接获取种子信息列表
     * <p>
     * 需要提供 RSS 链接和番剧 ID
     * <p>
     * 返回值：种子信息列表，如果获取失败或没有种子则返回 null
     */
    static
    List<Map<String, String>> parseNyaaRSS(RssReader reader, String rss_url) throws IOException
    {
        // 自建一个 Map 来保存每个 Item 对应的扩展字段
        Map<Item, Map<String, String>> itemExtensions = new IdentityHashMap<>();
        reader.addItemExtension("nyaa:size", (item, value) ->
            itemExtensions.computeIfAbsent(item, _ -> new HashMap<>()).put("size", value.trim())
        );

        // 读取一个 RSS 链接
        var items = reader.read(rss_url).toList();

        // 遍历 RSS 条目，提取种子信息
        List<Map<String, String>> res = new ArrayList<>();
        for(var item : items)
        {
            // 填充信息
            Map<String, String> recode = new HashMap<>();
            res.add(recode);

            // 填充信息
            recode.put("TOR_URL", item.getLink().orElse(null));

            try
            {
                var rssFormat      = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", java.util.Locale.ENGLISH);
                var dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                // 解析字符串为 Date 转成标准格式字符串
                var air_datetime = dateTimeFormat.format(rssFormat.parse(item.getPubDate().orElse(null)));
                recode.put("air_datetime", air_datetime);
            }
            catch(ParseException _) { recode.put("air_datetime", null); }

            var ext  = itemExtensions.getOrDefault(item, Collections.emptyMap());
            var len  = ParseSizeToBytes(ext.getOrDefault("size", null));
            var size = len == null ? null : String.valueOf(len);
            recode.put("size", size);

            recode.put("url_page", item.getGuid().orElse(null));

            var title = item.getTitle().orElse(null);
            recode.put("title", title);
            recode.put("subtitle_group", ParseSubtitleGroup(title));

            recode.put("description", item.getDescription().orElse(null));
        }
        return res;
    }


    // 解析字幕组
    private static
    String ParseSubtitleGroup(String title)
    {
        String[] left_brackets  = {"[", "【", "(", "（"};
        String[] right_brackets = {"]", "】", ")", "）"};

        for(int i = 0; i < left_brackets.length; i++)
        {
            // 如果匹配到左括号
            if(title.startsWith(left_brackets[i]))
            {
                int end_index = title.indexOf(right_brackets[i]);         // 找到对应的右括号
                if(end_index != -1) return title.substring(1, end_index); // 如果找到了右括号，就提取中间的字幕组名称
            }
        }
        return null; // 如果没有匹配到任何括号，就返回 null
    }

    private static
    Long ParseSizeToBytes(String size_str)
    {
        if(size_str == null || size_str.isEmpty()) return null;

        size_str = size_str.trim().replace(",", ".");
        String[] parts = size_str.split(" ");
        if(parts.length != 2) return null;

        double value = Double.parseDouble(parts[0]);
        String unit  = parts[1].toUpperCase();
        return switch(unit)
        {
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
