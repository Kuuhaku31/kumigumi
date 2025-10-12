// TestAPI.java

package test;

import NetAccess.BangumiAPI;
import org.json.JSONArray;
import org.json.JSONObject;
import utils.EpisodeInfo;

public
class TestAPI
{
    static
    void main(String[] args)
    {
        String query_type_str = args[0];
        int anime_id = Integer.parseInt(args[1]);

        BangumiAPI.QueryType query_type = BangumiAPI.ParseQueryType(query_type_str);

        // 获取番剧信息
        if(query_type == null)
        {
            System.err.println("Error: 未知的查询类型");
            return;
        }
        String res = BangumiAPI.GetInfo(BangumiAPI.QueryType.episode_list, anime_id);
        JSONObject info_list = null;
        if(res != null) info_list = new JSONObject(res);

        // 处理 json 格式返回结果
        // AnimeInfo anime_info = BangumiAPI.ParseAnimeInfo(anime_info_json);
        // if(anime_info != null)
        // {
        //     anime_info.PrintInfo();
        // }

        JSONArray ep_list = null;
        if(info_list != null)
        {
            ep_list = info_list.getJSONArray("data");
        }

        if(ep_list != null)
        {
            for(int i = 0; i < ep_list.length(); i++)
            {
                JSONObject ep_info_json = ep_list.getJSONObject(i);
                EpisodeInfo episode_info = BangumiAPI.ParseEpisodeInfo(ep_info_json);
                if(episode_info != null)
                {
                    IO.println("================================");
                    episode_info.PrintInfo();
                }
            }
        }
    }

}
