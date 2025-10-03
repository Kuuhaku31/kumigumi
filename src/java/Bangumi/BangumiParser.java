// Bangumi.java

package Bangumi;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import utils.AnimeInfo;
import utils.EpisodeInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public
class BangumiParser
{
    public static
    BangumiPageInfo
    ParseBangumiHtml(String html)
    {
        Document doc = Jsoup.parse(html);
        AnimeInfo animeInfo = new AnimeInfo();
        List<EpisodeInfo> episodeList = new ArrayList<>();

        // 番组bangumi链接、原名、译名、话数
        Element h1a = doc.selectFirst("h1 a");
        if(h1a != null)
        {
            animeInfo.info.put("番组bangumi链接", "https://bangumi.tv" + h1a.attr("href"));
            animeInfo.info.put("番组原名", h1a.text());
            animeInfo.info.put("番组译名", h1a.text());
            animeInfo.info.put("番组话数", "*");
        }

        // ul#infobox li
        Elements infoboxLis = doc.select("ul#infobox > li");
        for(Element li : infoboxLis)
        {
            Element tip = li.selectFirst("span.tip");
            if(tip == null) continue;
            String key = tip.text().replace(":", "").trim();
            key = switch(key)
            {
                case "中文名" -> "番组译名";
                case "别名" -> "番组别名";
                case "话数" -> "番组话数";
                case "放送开始", "上映年度" -> "发布日期";
                default -> key;
            };
            String value;
            Elements links = li.select("a");
            Elements subUl = li.select("ul li");
            if(!links.isEmpty())
            {
                if("官方网站".equals(key))
                {
                    key = "番组官网链接";
                    value = Objects.requireNonNull(links.first()).attr("href");
                }
                else
                {
                    value = String.join(" / ", links.eachText());
                }
            }
            else if(!subUl.isEmpty())
            {
                value = String.join(" / ", subUl.eachText());
            }
            else
            {
                value = li.text().replace(tip.text(), "").trim();
            }
            animeInfo.info.put(key, value);
        }

        // 封面
        Element cover = doc.selectFirst("img.cover");
        if(cover != null)
        {
            animeInfo.info.put("番组封面链接", "https:" + cover.attr("src"));
        }

        // 单集信息
        Elements prgLis = doc.select("ul.prg_list > li");
        for(Element prg : prgLis)
        {
            Element aTag = prg.selectFirst("a");
            if(aTag == null) continue;
            EpisodeInfo ep = new EpisodeInfo();
            ep.info.put("话bangumiURL", "https://bangumi.tv" + aTag.attr("href"));
            ep.info.put("番组bangumi链接", animeInfo.info.get("番组bangumi链接"));
            ep.info.put("话索引", aTag.text());
            String[] titleSplit = aTag.attr("title").split(" ", 2);
            ep.info.put("话标题", titleSplit.length > 1 ? titleSplit[1] : "");

            // 通过id获取span标签
            String relId = aTag.attr("rel").replace("#", "");
            Element span = doc.selectFirst("div#" + relId + " > span");
            if(span != null)
            {
                String tipText = span.html().replaceAll("<span.*?>|</span>|\\s|\\n", "").trim();
                String[] lines = tipText.split("<br/>");
                for(String line : lines)
                {
                    if(!line.contains(":")) continue;
                    String[] kv = line.split(":", 2);
                    switch(kv[0])
                    {
                        case "中文标题" -> ep.info.put("话标题译名", kv[1]);
                        case "首播" -> ep.info.put("发布日期", kv[1]);
                        case "时长" -> ep.info.put("话时长", kv[1]);
                    }
                }
            }
            episodeList.add(ep);
        }

        return new BangumiPageInfo(animeInfo, episodeList);
    }

}
