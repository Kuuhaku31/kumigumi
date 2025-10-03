// MikanParser.java

package NetAccess.MikanAnime;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import utils.TorrentInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public
class MikanParser
{
    public static
    List<TorrentInfo> ParseMikanRssXML(String bangumiUrl, String rssHtmlStr)
    {
        List<TorrentInfo> result = new ArrayList<>();
        Document doc = Jsoup.parse(rssHtmlStr, "", org.jsoup.parser.Parser.xmlParser());
        Elements items = doc.select("item");
        for(Element item : items)
        {
            Map<String, String> info = new HashMap<>();
            info.put("番组bangumi链接", bangumiUrl);
            info.put("种子下载链接", item.selectFirst("enclosure") != null ? item.selectFirst("enclosure").attr("url") : "");
            info.put("种子标题", item.selectFirst("title") != null ? item.selectFirst("title").text() : "");
            info.put("发布日期", item.selectFirst("torrent > pubdate") != null ? item.selectFirst("torrent > pubdate").text() : "");
            info.put("种子页面链接", item.selectFirst("link") != null ? item.selectFirst("link").text() : "");
            info.put("种子描述", item.selectFirst("description") != null ? item.selectFirst("description").text() : "");

            // 字幕组提取
            String title = info.get("种子标题");
            String groupName = "未知字幕组";
            if(title != null && !title.isEmpty())
            {
                if(title.startsWith("["))
                {
                    groupName = title.split("]")[0].substring(1);
                }
                else if(title.startsWith("【"))
                {
                    groupName = title.split("】")[0].substring(1);
                }
            }
            info.put("种子字幕组", groupName);

            // 大小提取
            Element contentLengthElem = item.selectFirst("torrent > contentlength");
            long contentLengthLong = contentLengthElem != null ? Long.parseLong(contentLengthElem.text()) : -1;
            info.put("种子大小_字节", String.valueOf(contentLengthLong));

            String contentLengthStr = getString(contentLengthLong);
            info.put("种子大小", contentLengthStr);

            // 新建 TorrentInfo 并添加到结果列表
            TorrentInfo torrentInfo = new TorrentInfo();
            torrentInfo.info = info;
            result.add(torrentInfo);
        }
        return result;
    }

    private static
    String getString(long contentLengthLong)
    {
        String contentLengthStr = "-";
        if(contentLengthLong >= 0)
        {
            if(contentLengthLong < 1024)
            {
                contentLengthStr = contentLengthLong + " B";
            }
            else if(contentLengthLong < 1024 * 1024)
            {
                contentLengthStr = String.format("%.2f KB", contentLengthLong / 1024.0);
            }
            else if(contentLengthLong < 1024 * 1024 * 1024)
            {
                contentLengthStr = String.format("%.2f MB", contentLengthLong / (1024.0 * 1024));
            }
            else
            {
                contentLengthStr = String.format("%.2f GB", contentLengthLong / (1024.0 * 1024 * 1024));
            }
        }
        return contentLengthStr;
    }
}
