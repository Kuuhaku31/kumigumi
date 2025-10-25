// BangumiAPI.java

package NetAccess;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;


public
class BangumiAPI
{
    private static final String bangumi_server = "https://api.bgm.tv";

    public static
    String[] GetAnimeData(int anime_id) throws URISyntaxException, IOException
    {
        return ParseAnimeInfo(GetInfo(BangumiAPI.QueryType.anime_info, anime_id));
    }
    
    public static
    String[][] GetEpisodeData(int anime_id) throws URISyntaxException, IOException
    {
        // 解析 episode 信息
        JSONObject ep_info_list = BangumiAPI.GetInfo(BangumiAPI.QueryType.episode_list, anime_id);

        JSONArray ep_list = ep_info_list.getJSONArray("data");
        if(ep_list == null) return new String[0][0];

        String[][] ep_data = new String[ep_list.length()][0];
        for(int i = 0; i < ep_list.length(); i++) ep_data[i] = ParseEpisodeInfo(ep_list.getJSONObject(i));
        return ep_data;
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

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-M-d");
        try
        {
            // 尝试解析为日期
            LocalDate.parse(str, formatter);
            return str; // ✅ 合法，原样返回
        }
        catch(DateTimeParseException e)
        {
            return null; // ❌ 非法格式或日期
        }
    }

    private static
    String[] ParseEpisodeInfo(JSONObject episode_info_json)
    {
        // 处理 json 格式字符串
        String ANI_ID = String.valueOf(episode_info_json.getInt("subject_id"));
        String EPI_ID = String.valueOf(episode_info_json.getInt("id"));

        // EpisodeInfo episode_info = new EpisodeInfo(ep_id, ani_id);

        // 解析放送日期
        String air_date = ValidateDate(episode_info_json.getString("airdate"));

        // 解析集数
        String ep_str   = episode_info_json.getNumber("ep").toString();
        String sort_str = episode_info_json.getNumber("sort").toString();
        String index    = ep_str.equals("0") ? "SP: " + sort_str : sort_str;

        // 解析标题
        String title    = episode_info_json.getString("name");
        String title_cn = episode_info_json.getString("name_cn");

        // 解析时长
        String duration = episode_info_json.getString("duration");

        // 解析概述
        String description = episode_info_json.getString("desc");

        // 返回
        return new String[] {EPI_ID, ANI_ID, air_date, duration, index, title, title_cn, description};
    }

    private static
    String[] ParseAnimeInfo(JSONObject anime_info_json)
    {
        // 解析 ANI_ID
        String ANI_ID = String.valueOf(anime_info_json.getInt("id"));

        // 解析放送日期
        String air_date = ValidateDate(anime_info_json.getString("date"));

        // 解析标题
        String title    = anime_info_json.getString("name");
        String title_cn = anime_info_json.getString("name_cn");

        // 解析别名
        Object aliases_json = parse_info_box(anime_info_json, "别名");
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

        // 解析集数
        String episode_count = String.valueOf(anime_info_json.getInt("eps"));

        // 解析官方网站
        Object official_site_json = parse_info_box(anime_info_json, "官方网站");
        String url_official_site  = official_site_json == null ? null : official_site_json.toString();

        // 解析封面图片
        String url_cover = anime_info_json.getJSONObject("images").getString("large");

        // 返回
        return new String[] {ANI_ID, air_date, title, title_cn, aliases, episode_count, url_official_site, url_cover};
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
        URL               url  = new URI(url_str).toURL();                  // 创建URL对象
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();  // 打开连接
        conn.setRequestMethod("GET");                                       // 设置请求方法
        conn.setRequestProperty("User-Agent", "Mozilla/5.0");               // 添加 User-Agent

        // 读取响应
        BufferedReader in       = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder  response = new StringBuilder();

        // 逐行读取响应内容
        String input_line;
        while((input_line = in.readLine()) != null) response.append(input_line);

        in.close();

        return response.toString();
    }


    private
    enum QueryType
    {
        anime_info,
        episode_list,
    }
}
