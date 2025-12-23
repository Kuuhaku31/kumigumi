package NetAccess;

import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.SignStyle;
import java.time.temporal.ChronoField;
import java.util.HashMap;
import java.util.Map;

class BangumiParser {
    static Map<String, String> ParseAnimeInfo(JSONObject anime_info_json) {
        var ANI_ID = String.valueOf(anime_info_json.getInt("id")); // 解析 ANI_ID
        var air_date = ValidateDate(anime_info_json.optString("date")); // 解析放送日期

        var title = anime_info_json.optString("name"); // 解析标题
        var title_cn = anime_info_json.optString("name_cn");
        if (title.isBlank())
            title = null;
        if (title_cn.isBlank())
            title_cn = null;

        // 解析别名
        var aliases_json = parse_info_box(anime_info_json, "别名");
        String aliases;
        if (aliases_json instanceof JSONArray aliases_array) {
            var sb = new StringBuilder();
            for (var i = 0; i < aliases_array.length(); i++) {
                if (i > 0)
                    sb.append(";");
                sb.append(aliases_array.getJSONObject(i).getString("v"));
            }
            aliases = sb.toString();
        } else if (aliases_json != null) {
            var str = aliases_json.toString();
            if (str.isBlank())
                aliases = null;
            else
                aliases = aliases_json.toString();
        } else
            aliases = null;

        // 解析 description
        var description = anime_info_json.optString("summary");
        if (description.isBlank())
            description = null;

        // 解析集数
        var count = anime_info_json.optInt("eps", -1);
        var episode_count = count == -1 ? null : String.valueOf(count);

        // 解析官方网站
        var official_site_json = parse_info_box(anime_info_json, "官方网站");
        var url_official_site = official_site_json == null ? null : official_site_json.toString();
        url_official_site = (url_official_site != null && url_official_site.isBlank()) ? null : url_official_site;

        // 解析封面图片
        var url_cover = anime_info_json.getJSONObject("images").getString("large");
        if (url_cover.isBlank())
            url_cover = null;

        // 返回
        Map<String, String> res = new HashMap<>();
        res.put("ANI_ID", ANI_ID);
        res.put("air_date", air_date);
        res.put("title", title);
        res.put("title_cn", title_cn);
        res.put("aliases", aliases);
        res.put("description", description);
        res.put("episode_count", episode_count);
        res.put("url_official_site", url_official_site);
        res.put("url_cover", url_cover);
        return res;
    }

    static Map<String, String> ParseEpisodeInfo(JSONObject episode_info_json) {
        var ANI_ID = String.valueOf(episode_info_json.getInt("subject_id"));
        var EPI_ID = String.valueOf(episode_info_json.getInt("id"));
        var air_date = ValidateDate(episode_info_json.getString("airdate"));

        // 解析集数
        var ep_str = episode_info_json.getNumber("ep").toString();
        var sort = episode_info_json.getNumber("sort").toString();
        // var index = ep_str.equals("0") ? "SP: " + sort : sort;

        // 解析标题
        var title = episode_info_json.getString("name");
        var title_cn = episode_info_json.getString("name_cn");
        if (title.isBlank())
            title = null;
        if (title_cn.isBlank())
            title_cn = null;

        // 解析时长
        var duration_seconds = episode_info_json.optInt("duration_seconds", 0);
        var duration = duration_seconds == 0 ? null : String.valueOf(duration_seconds);

        // 解析概述
        var description = episode_info_json.optString("desc");
        if (description.isBlank())
            description = null;

        // 返回
        Map<String, String> res = new HashMap<>();
        res.put("EPI_ID", EPI_ID);
        res.put("ANI_ID", ANI_ID);
        res.put("sort", sort);
        res.put("air_date", air_date);
        res.put("duration", duration);
        res.put("ep", ep_str);
        res.put("title", title);
        res.put("title_cn", title_cn);
        res.put("description", description);
        return res;
    }

    private static String ValidateDate(String str) {
        if (str == null || str.isBlank())
            return null;

        // 尝试解析为日期
        var formatter = new DateTimeFormatterBuilder()
                .appendValue(ChronoField.YEAR, 4)
                .appendLiteral('-')
                .appendValue(ChronoField.MONTH_OF_YEAR, 1, 2, SignStyle.NOT_NEGATIVE)
                .appendLiteral('-')
                .appendValue(ChronoField.DAY_OF_MONTH, 1, 2, SignStyle.NOT_NEGATIVE)
                .toFormatter();
        try {
            LocalDate.parse(str, formatter);
            return str; // ✅ 合法，原样返回
        } catch (DateTimeParseException _) {
            return null;
        } // ❌ 非法格式或日期
    }

    // 获取 info_box 中指定 key 的项
    private static Object parse_info_box(JSONObject anime_info_json, String key) {
        if (anime_info_json == null || !anime_info_json.has("infobox"))
            return null;

        JSONArray infobox = anime_info_json.optJSONArray("infobox");
        if (infobox == null)
            return null;

        for (int i = 0; i < infobox.length(); i++) {
            JSONObject item = infobox.getJSONObject(i);
            if (item != null && key.equals(item.getString("key"))) {
                // 用 opt() 返回一个通用的 Object，先取出它，再判断类型
                return item.opt("value");
            }
        }
        return null;
    }
}
