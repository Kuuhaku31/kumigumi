package NetAccess;


import java.util.Locale;


enum RSSSourceType {

    MIKAN,
    NYAA,
    UNKNOWN;

    /**
     * 根据 RSS URL 前缀检测来源。
     */
    static RSSSourceType detectRSSSourceType(String rss_url) {

        var normalized_url = rss_url == null ? "" : rss_url.toLowerCase(Locale.ROOT);

        if(normalized_url.startsWith("https://mikanani.me") || normalized_url.startsWith("https://mikan.tangbai.cc")) {
            return RSSSourceType.MIKAN;
        }
        else if(normalized_url.startsWith("https://nyaa")) {
            return RSSSourceType.NYAA;
        }
        return RSSSourceType.UNKNOWN;
    }
}
