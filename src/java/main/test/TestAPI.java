// TestAPI.java

package test;

import NetAccess.BangumiAPI;
import utils.AnimeInfo;

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
        String res = BangumiAPI.GetInfo(BangumiAPI.QueryType.anime_info, anime_id);

        // 处理 json 格式返回结果
        AnimeInfo anime_info = BangumiAPI.ParseAnimeInfo(res);
        if(anime_info != null)
        {
            anime_info.PrintInfo();
        }
    }

}
