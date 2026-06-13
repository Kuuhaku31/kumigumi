package NetAccess;

import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.SignStyle;
import java.time.temporal.ChronoField;
import java.util.ArrayList;

import Utils.UtilityFunctions;

import org.json.JSONArray;
import org.json.JSONObject;

import Database.Info.AnimeInfo;
import Database.Info.EpisodeInfo;


final class BangumiParser {

    private static final DateTimeFormatter BANGUMI_DATE_FORMATTER = new DateTimeFormatterBuilder()
        .appendValue(ChronoField.YEAR, 4)
        .appendLiteral('-')
        .appendValue(ChronoField.MONTH_OF_YEAR, 1, 2, SignStyle.NOT_NEGATIVE)
        .appendLiteral('-')
        .appendValue(ChronoField.DAY_OF_MONTH, 1, 2, SignStyle.NOT_NEGATIVE)
        .toFormatter();

    private BangumiParser() {}

    static AnimeInfo parseAnimeInfo(JSONObject anime_info_json) {
        if(anime_info_json == null) throw new IllegalArgumentException("Bangumi 番剧 JSON 不能为空");

        return new AnimeInfo(
            nullableInt(anime_info_json, "id"),
            parseBangumiDate(nullableString(anime_info_json, "date")),
            nullableString(anime_info_json, "name"),
            nullableString(anime_info_json, "name_cn"),
            parseAliases(anime_info_json),
            nullableString(anime_info_json, "summary"),
            nullableInt(anime_info_json, "eps"),
            parseOfficialSite(anime_info_json),
            parseCoverUrl(anime_info_json));
    }

    static EpisodeInfo parseEpisodeInfo(JSONObject episode_info_json) {
        if(episode_info_json == null) throw new IllegalArgumentException("Bangumi 分集 JSON 不能为空");

        // duration_seconds 小于等于 0 时视为缺省值。
        var duration_seconds = episode_info_json.optInt("duration_seconds", 0);
        var duration         = duration_seconds <= 0 ? null : duration_seconds;

        return new EpisodeInfo(
            nullableInt(episode_info_json, "id"),
            nullableInt(episode_info_json, "subject_id"),
            nullableInt(episode_info_json, "ep"),
            nullableDouble(episode_info_json, "sort"),
            parseBangumiDate(nullableString(episode_info_json, "airdate")),
            duration,
            nullableString(episode_info_json, "name"),
            nullableString(episode_info_json, "name_cn"),
            nullableString(episode_info_json, "desc"));
    }


    private static Integer nullableInt(JSONObject json, String key) {
        var number = json.optNumber(key, null);
        return number == null ? null : number.intValue();
    }

    private static Double nullableDouble(JSONObject json, String key) {
        var number = json.optNumber(key, null);
        return number == null ? null : number.doubleValue();
    }

    private static String nullableString(JSONObject json, String key) {
        if(!json.has(key) || json.isNull(key)) return null;
        return blankToNull(json.optString(key, null));
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    // Bangumi 日期可能省略月份或日期的前导 0；这里校验后统一转成 yyyy-MM-dd。
    private static java.util.Date parseBangumiDate(String date_str) {
        if(date_str == null) return null;

        try {
            var normalized_date = LocalDate.parse(date_str, BANGUMI_DATE_FORMATTER).toString();
            return UtilityFunctions.parseDate(normalized_date);
        } catch(DateTimeParseException ignored) {
            return null;
        }
    }

    private static String parseAliases(JSONObject anime_info_json) {
        var aliases_json = infoBoxValue(anime_info_json, "别名");

        if(aliases_json instanceof JSONArray aliases_array) {
            var aliases = new ArrayList<String>();
            for(var i = 0; i < aliases_array.length(); i++) {
                var alias_item = aliases_array.optJSONObject(i);
                if(alias_item != null) {
                    var alias = nullableString(alias_item, "v");
                    if(alias != null) aliases.add(alias);
                }
            }
            return aliases.isEmpty() ? null : String.join(";", aliases);
        }

        return jsonValueToNullableString(aliases_json);
    }

    private static String parseOfficialSite(JSONObject anime_info_json) {
        return jsonValueToNullableString(infoBoxValue(anime_info_json, "官方网站"));
    }

    private static String parseCoverUrl(JSONObject anime_info_json) {
        var images_json = anime_info_json.optJSONObject("images");
        return images_json == null ? null : nullableString(images_json, "large");
    }

    // Bangumi 的 infobox 是 key/value 数组；调用方再按 value 的具体类型解析。
    private static Object infoBoxValue(JSONObject anime_info_json, String key) {
        var infobox = anime_info_json.optJSONArray("infobox");
        if(infobox == null) return null;

        for(var i = 0; i < infobox.length(); i++) {
            var item = infobox.optJSONObject(i);
            if(item != null && key.equals(item.optString("key"))) return item.opt("value");
        }
        return null;
    }

    private static String jsonValueToNullableString(Object value) {
        if(value == null || JSONObject.NULL.equals(value)) return null;
        return blankToNull(value.toString());
    }
}
