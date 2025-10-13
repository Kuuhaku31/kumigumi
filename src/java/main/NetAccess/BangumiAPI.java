// BangumiAPI.java

package NetAccess;

import org.json.JSONArray;
import org.json.JSONObject;
import utils.BangumiInfoSet;
import utils.Info.AnimeInfo;
import utils.Info.EpisodeInfo;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;


public
class BangumiAPI
{
    private static final String bangumi_server = "https://api.bgm.tv";

    public static
    BangumiInfoSet GetBangumiInfoSet(int anime_id) throws URISyntaxException, IOException
    {
        BangumiInfoSet bangumi_info_set = new BangumiInfoSet();

        // 获取番剧信息和剧集json信息
        JSONObject ani_info_json = BangumiAPI.GetInfo(BangumiAPI.QueryType.anime_info, anime_id);
        JSONObject ep_info_list = BangumiAPI.GetInfo(BangumiAPI.QueryType.episode_list, anime_id);

        // 解析 anime 信息
        bangumi_info_set.anime_info = BangumiAPI.ParseAnimeInfo(ani_info_json);

        // 解析 episode 信息
        JSONArray ep_list = ep_info_list.getJSONArray("data");
        if(ep_list != null) for(int i = 0; i < ep_list.length(); i++)
        {
            // 解析单个剧集信息
            JSONObject ep_info_json = ep_list.getJSONObject(i);
            EpisodeInfo episode_info = BangumiAPI.ParseEpisodeInfo(ep_info_json);

            // 添加到列表
            bangumi_info_set.episode_info_list.add(episode_info);
        }

        return bangumi_info_set;
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
        String res_str = Net.Get(url_str);

        return new JSONObject(res_str);
    }

    private static
    EpisodeInfo ParseEpisodeInfo(JSONObject episode_info_json)
    {
        // 处理 json 格式字符串
        EpisodeInfo episode_info = new EpisodeInfo();

        // 解析番剧ID
        episode_info.ani_id = episode_info_json.getInt("subject_id");

        // 解析剧集ID
        episode_info.ep_id = episode_info_json.getInt("id");

        // 解析放送日期
        String date_str = episode_info_json.getString("airdate");
        episode_info.air_date = LocalDate.parse(date_str, DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        // 解析集数
        String ep_str = episode_info_json.getNumber("ep").toString();
        String sort_str = episode_info_json.getNumber("sort").toString();
        episode_info.index = ep_str.equals("0") ? "SP: " + sort_str : sort_str;

        // 解析标题
        episode_info.title = episode_info_json.getString("name");
        episode_info.title_cn = episode_info_json.getString("name_cn");

        // 解析时长
        String duration_str = episode_info_json.getString("duration");
        if(!duration_str.isEmpty()) episode_info.duration = LocalTime.parse(duration_str);

        // 解析概述
        episode_info.description = episode_info_json.getString("desc");

        return episode_info;
    }


    private static
    AnimeInfo ParseAnimeInfo(JSONObject anime_info_json)
    {
        // 处理 json 格式字符串
        AnimeInfo anime_info = new AnimeInfo();

        // 解析ID
        anime_info.ani_id = anime_info_json.getInt("id");

        // 解析放送日期
        String date_str = anime_info_json.getString("date");
        anime_info.air_date = LocalDate.parse(date_str, DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        // 解析标题
        anime_info.title = anime_info_json.getString("name");
        anime_info.title_cn = anime_info_json.getString("name_cn");

        // 解析别名
        Object aliases_json = parse_info_box(anime_info_json, "别名");
        String aliases_str;
        if(aliases_json instanceof JSONArray aliases_array)
        {
            StringBuilder sb = new StringBuilder();
            for(int i = 0; i < aliases_array.length(); i++)
            {
                if(i > 0) sb.append(";");
                sb.append(aliases_array.getJSONObject(i).getString("v"));
            }
            aliases_str = sb.toString();
        }
        else if(aliases_json != null) aliases_str = aliases_json.toString();
        else aliases_str = "";
        anime_info.aliases = aliases_str;

        // 解析集数
        anime_info.episode_count = anime_info_json.getInt("eps");

        // 解析官方网站
        Object official_site_json = parse_info_box(anime_info_json, "官方网站");
        if(official_site_json != null) anime_info.official_site_url = official_site_json.toString();

        // 解析封面图片
        anime_info.cover_url = anime_info_json.getJSONObject("images").getString("large");

        return anime_info;
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


    private
    enum QueryType
    {
        anime_info,
        episode_list,
    }
}
