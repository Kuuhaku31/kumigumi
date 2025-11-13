// BangumiAPI.java

package NetAccess;

import org.json.JSONArray;
import org.json.JSONObject;
import utils.TableData.TableData;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;


public
class BangumiAPI
{
    private static final String bangumi_server = "https://api.bgm.tv";

    public static
    void GetAnimeData(TableData data, int anime_id) throws URISyntaxException, IOException
    {
        ParseAnimeInfo(data.new Record(), GetInfo(QueryType.anime_info, anime_id));
    }

    public static
    void GetEpisodeData(TableData data, int anime_id) throws URISyntaxException, IOException
    {
        // 解析 episode 信息
        var ep_list = GetInfo(QueryType.episode_list, anime_id).getJSONArray("data");
        if(ep_list == null) return;

        for(int i = 0; i < ep_list.length(); i++) ParseEpisodeInfo(data.new Record(), ep_list.getJSONObject(i));
    }


    private static
    void ParseEpisodeInfo(TableData.Record record, JSONObject episode_info_json)
    {
        var ANI_ID   = String.valueOf(episode_info_json.getInt("subject_id"));
        var EPI_ID   = String.valueOf(episode_info_json.getInt("id"));
        var air_date = ValidateDate(episode_info_json.getString("airdate"));

        // 解析集数
        var ep_str = episode_info_json.getNumber("ep").toString();
        var sort   = episode_info_json.getNumber("sort").toString();
        var index  = ep_str.equals("0") ? "SP: " + sort : sort;

        // 解析标题
        var title    = episode_info_json.getString("name");
        var title_cn = episode_info_json.getString("name_cn");

        // 解析时长
        var duration_seconds = episode_info_json.optInt("duration_seconds", -1);
        var duration         = duration_seconds == -1 ? null : String.valueOf(duration_seconds);

        // 解析概述
        var description = episode_info_json.optString("desc");

        // 返回
        // return new String[] {EPI_ID, ANI_ID, air_date, duration, index, title, title_cn, description};
        record.Set("EPI_ID", EPI_ID);
        record.Set("ANI_ID", ANI_ID);
        record.Set("sort", sort);
        record.Set("air_date", air_date);
        record.Set("duration", duration);
        record.Set("ep", index);
        record.Set("title", title);
        record.Set("title_cn", title_cn);
        record.Set("description", description);
    }

    private static
    void ParseAnimeInfo(TableData.Record record, JSONObject anime_info_json)
    {

        var ANI_ID   = String.valueOf(anime_info_json.getInt("id"));    // 解析 ANI_ID
        var air_date = ValidateDate(anime_info_json.optString("date")); // 解析放送日期
        var title    = anime_info_json.optString("name");               // 解析标题
        var title_cn = anime_info_json.optString("name_cn");

        // 解析别名
        var    aliases_json = parse_info_box(anime_info_json, "别名");
        String aliases;
        if(aliases_json instanceof JSONArray aliases_array)
        {
            StringBuilder sb = new StringBuilder();
            for(int i = 0; i < aliases_array.length(); i++)
            {
                if(i > 0) sb.append(";");
                sb.append(aliases_array.getJSONObject(i).getString("v"));
            }
            aliases = sb.toString();
        }
        else if(aliases_json != null) aliases = aliases_json.toString();
        else aliases = null;

        // 解析 description
        var description = anime_info_json.optString("summary");

        // 解析集数
        var count         = anime_info_json.optInt("eps", -1);
        var episode_count = count == -1 ? null : String.valueOf(count);

        // 解析官方网站
        var official_site_json = parse_info_box(anime_info_json, "官方网站");
        var url_official_site  = official_site_json == null ? null : official_site_json.toString();

        // 解析封面图片
        var url_cover = anime_info_json.getJSONObject("images").getString("large");

        // return new String[] {ANI_ID, air_date, title, title_cn, aliases, episode_count, url_official_site, url_cover};
        record.Set("ANI_ID", ANI_ID);
        record.Set("air_date", air_date);
        record.Set("title", title);
        record.Set("title_cn", title_cn);
        record.Set("aliases", aliases);
        record.Set("description", description);
        record.Set("episode_count", episode_count);
        record.Set("url_official_site", url_official_site);
        record.Set("url_cover", url_cover);
    }


    private static
    JSONObject GetInfo(QueryType type, int anime_id) throws URISyntaxException, IOException
    {
        String format_str;
        switch(type)
        {
        case anime_info -> format_str = "%s/v0/subjects/%d";
        case episode_list -> format_str = "%s/v0/episodes?subject_id=%d";
        default -> throw new UnsupportedOperationException("BangumiAPI GetInfo: 未知的查询类型");
        }
        String url_str = String.format(format_str, bangumi_server, anime_id);
        String res_str = Get(url_str);

        return new JSONObject(res_str);
    }

    private static
    String ValidateDate(String str)
    {
        if(str == null || str.isBlank()) return null;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        try // 尝试解析为日期
        {
            LocalDate.parse(str, formatter);
            return str; // ✅ 合法，原样返回
        }
        catch(DateTimeParseException _) { return null; } // ❌ 非法格式或日期
    }

    // 获取 info_box 中指定 key 的项
    private static
    Object parse_info_box(JSONObject anime_info_json, String key)
    {
        if(anime_info_json == null || !anime_info_json.has("infobox")) return null;

        JSONArray infobox = anime_info_json.optJSONArray("infobox");
        if(infobox == null) return null;

        for(int i = 0; i < infobox.length(); i++)
        {
            JSONObject item = infobox.getJSONObject(i);
            if(item != null && key.equals(item.getString("key")))
            {
                // 用 opt() 返回一个通用的 Object，先取出它，再判断类型
                return item.opt("value");
            }
        }
        return null;
    }

    private static
    String Get(String url_str) throws URISyntaxException, IOException
    {
        var url  = new URI(url_str).toURL();                  // 创建URL对象
        var conn = (HttpURLConnection) url.openConnection();  // 打开连接
        conn.setRequestMethod("GET");                         // 设置请求方法
        conn.setRequestProperty("User-Agent", "Mozilla/5.0"); // 添加 User-Agent

        // 读取响应
        var in       = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        var response = new StringBuilder();

        // 逐行读取响应内容
        String input_line;
        while((input_line = in.readLine()) != null) response.append(input_line);

        in.close();

        return response.toString();
    }


    // 请求类型
    private
    enum QueryType
    {
        anime_info,
        episode_list,
    }
}
