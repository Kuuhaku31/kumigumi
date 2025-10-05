package utils;

import java.util.HashMap;
import java.util.Map;

/**
 * 字段一览
 * <table>
 *   <tr><th>中文键名</th><th>英文键名</th><th>备注</th></tr>
 *   <tr><td>番组bangumi链接</td><td>anime_bangumi_url</td><td>bangumi网页链接</td></tr>
 *   <tr><td>番组RSS订阅链接</td><td>anime_rss_url</td><td>rss订阅源链接</td></tr>
 *   <tr><td>番组原名</td><td>anime_title</td><td></td></tr>
 *   <tr><td>番组译名</td><td>anime_title_cn</td><td></td></tr>
 *   <tr><td>番组别名</td><td>anime_aliases</td><td></td></tr>
 *   <tr><td>番组话数</td><td>anime_episode_count</td><td></td></tr>
 *   <tr><td>番组官网链接</td><td>anime_official_site_url</td><td></td></tr>
 *   <tr><td>番组封面链接</td><td>anime_cover_url</td><td></td></tr>
 *   <tr><td>番组观前评分</td><td>anime_pre_view_rating</td><td></td></tr>
 *   <tr><td>番组观后评分</td><td>anime_after_view_rating</td><td></td></tr>
 *   <tr><td>话bangumiURL</td><td>episode_bangumi_url</td><td></td></tr>
 *   <tr><td>话索引</td><td>episode_index</td><td></td></tr>
 *   <tr><td>话标题</td><td>episode_title</td><td></td></tr>
 *   <tr><td>话标题译名</td><td>episode_title_cn</td><td></td></tr>
 *   <tr><td>话时长</td><td>episode_duration</td><td></td></tr>
 *   <tr><td>话下载情况</td><td>episode_download_status</td><td>单集下载情况</td></tr>
 *   <tr><td>话观看情况</td><td>episode_view_status</td><td>单集观看情况</td></tr>
 *   <tr><td>种子下载链接</td><td>torrent_download_url</td><td></td></tr>
 *   <tr><td>种子页面链接</td><td>torrent_page_url</td><td></td></tr>
 *   <tr><td>种子字幕组</td><td>torrent_subtitle_group</td><td></td></tr>
 *   <tr><td>种子标题</td><td>torrent_title</td><td></td></tr>
 *   <tr><td>种子描述</td><td>torrent_description</td><td></td></tr>
 *   <tr><td>种子大小</td><td>torrent_size</td><td>种子文件大小</td></tr>
 *   <tr><td>种子大小_字节</td><td>torrent_size_bytes</td><td>种子文件大小（字节）</td></tr>
 *   <tr><td>种子下载情况</td><td>torrent_download_status</td><td>种子下载情况</td></tr>
 *   <tr><td>发布日期</td><td>air_date</td><td></td></tr>
 *   <tr><td>备注</td><td>note</td><td></td></tr>
 * </table>
 */
public
class Translate
{
    // 中文键名到英文键名的映射
    private static final Map<String, String> map = new HashMap<>();

    static
    {
        map.put("番组bangumi链接", "anime_bangumi_url");
        map.put("番组RSS订阅链接", "anime_rss_url");
        map.put("番组原名", "anime_title");
        map.put("番组译名", "anime_title_cn");
        map.put("番组别名", "anime_aliases");
        map.put("番组话数", "anime_episode_count");
        map.put("番组官网链接", "anime_official_site_url");
        map.put("番组封面链接", "anime_cover_url");
        map.put("番组观前评分", "anime_pre_view_rating");
        map.put("番组观后评分", "anime_after_view_rating");
        map.put("话bangumiURL", "episode_bangumi_url");
        map.put("话索引", "episode_index");
        map.put("话标题", "episode_title");
        map.put("话标题译名", "episode_title_cn");
        map.put("话时长", "episode_duration");
        map.put("话下载情况", "episode_download_status");
        map.put("话观看情况", "episode_view_status");
        map.put("种子下载链接", "torrent_download_url");
        map.put("种子页面链接", "torrent_page_url");
        map.put("种子字幕组", "torrent_subtitle_group");
        map.put("种子标题", "torrent_title");
        map.put("种子描述", "torrent_description");
        map.put("种子大小", "torrent_size");
        map.put("种子大小_字节", "torrent_size_bytes");
        map.put("种子下载情况", "torrent_download_status");
        map.put("发布日期", "air_date");
        map.put("备注", "note");
    }

    public static
    void PrintMap()
    {
        IO.println("Map 内容:");
        for(Map.Entry<String, String> entry : map.entrySet())
        {
            IO.println(entry.getKey() + " -> " + entry.getValue());
        }
    }

    // 单个键名翻译
    // 默认由中文翻译成英文
    public static
    String TranslateKey(String key)
    {
        // String value = map.get(key);
        return map.getOrDefault(key, key);
    }

    // 翻译 Map 的键名
    public static
    Map<String, String> TranslateMap(Map<String, String> original)
    {
        // 新建一个 Map 来存储翻译后的结果
        Map<String, String> translated = new HashMap<>();

        // 遍历原始 Map，翻译每个键名并存入新 Map
        for(Map.Entry<String, String> entry : original.entrySet())
        {
            String translatedKey = TranslateKey(entry.getKey());
            translated.put(translatedKey, entry.getValue());
        }
        return translated;
    }
}
