package NetAccess;

import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

import Database.TorrentPageInfo;
import Utils.UtilityFunctions;

import com.apptasticsoftware.rssreader.Item;
import com.apptasticsoftware.rssreader.RssReader;


final class RSSParser {

    private static final String NYAA_INFO_HASH_EXTENSION = "nyaa:infoHash";
    private static final String INFO_HASH_KEY            = "infoHash";

    private RSSParser() {}

    static TorrentPageInfo parseMikanItem(String rss_url, Item item) {
        var enclosure    = item.getEnclosure().orElseThrow(() -> new RuntimeException("Mikan RSS 条目缺少 enclosure"));
        var url_download = enclosure.getUrl();
        var title        = item.getTitle().orElse(null);

        return new TorrentPageInfo(
            rss_url,
            extractTorrentHash(url_download),
            parseMikanDateTime(item.getPubDate().orElse(null)),
            url_download,
            item.getLink().orElse(null),
            title,
            parseSubtitleGroup(title),
            item.getDescription().orElse(null));
    }

    static TorrentPageInfo parseNyaaItem(
        String                         rss_url,
        Item                           item,
        Map<Item, Map<String, String>> item_extensions) {
        var ext   = item_extensions.getOrDefault(item, Collections.emptyMap());
        var title = item.getTitle().orElse(null);

        return new TorrentPageInfo(
            rss_url,
            ext.getOrDefault(INFO_HASH_KEY, null),
            UtilityFunctions.parseOffsetDateTime(item.getPubDate().orElse(null)),
            item.getLink().orElse(null),
            item.getGuid().orElse(null),
            title,
            parseSubtitleGroup(title),
            item.getDescription().orElse(null));
    }

    static Map<Item, Map<String, String>> registerNyaaExtensions(RssReader reader) {
        Map<Item, Map<String, String>> item_extensions = new IdentityHashMap<>();
        reader.addItemExtension(
            NYAA_INFO_HASH_EXTENSION,
            (item, value) -> item_extensions.computeIfAbsent(item, ignored -> new HashMap<>()).put(INFO_HASH_KEY, value.trim()));
        return item_extensions;
    }

    private static String extractTorrentHash(String url_download) {
        if(url_download == null || url_download.isBlank()) return null;

        var file_name = url_download.substring(url_download.lastIndexOf("/") + 1);
        return file_name.replace(".torrent", "");
    }

    // Mikan pubDate 不带时区；按站点时间补 +08:00 后解析。
    private static OffsetDateTime parseMikanDateTime(String pub_date) {
        if(pub_date == null || pub_date.isBlank()) return null;

        try {
            return OffsetDateTime.parse(pub_date + "+08:00");
        } catch(DateTimeParseException ignored) {
            return null;
        }
    }

    // 从标题开头的括号中提取字幕组名，例如 "[Group] title" -> "Group"。
    private static String parseSubtitleGroup(String title) {
        if(title == null || title.isBlank()) return null;

        String[] left_brackets  = { "[", "【", "(", "（" };
        String[] right_brackets = { "]", "】", ")", "）" };

        for(int i = 0; i < left_brackets.length; i++) {
            if(title.startsWith(left_brackets[i])) {
                var end_index = title.indexOf(right_brackets[i]);
                if(end_index != -1) return title.substring(1, end_index);
            }
        }
        return null;
    }
}
