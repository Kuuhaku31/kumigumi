// TestAPI.java

package test;

import NetAccess.BangumiAPI;
import NetAccess.MikanRSS;
import utils.TorrentInfo;

import java.util.ArrayList;

public
class TestAPI
{
    static
    void main(String[] args)
    {
        String query_type_str = args[0];
        int anime_id = Integer.parseInt(args[2]);
        // int anime_id = 19112387;

        BangumiAPI.QueryType query_type = BangumiAPI.ParseQueryType(query_type_str);

        // 获取番剧信息
        if(query_type == null)
        {
            System.err.println("Error: 未知的查询类型");
            return;
        }

        // BangumiInfoSet bangumi_info_set = BangumiAPI.BangumiInfoSet(anime_id);
        // bangumi_info_set.PrintInfo();

        // 测试 mikan
        String rss_url = "https://mikanani.me/RSS/Bangumi?bangumiId=3698";
        ArrayList<TorrentInfo> torrent_info_list = MikanRSS.GetTorrentInfoList(rss_url, anime_id);

        IO.println("======== Torrent Info List =========");
        for(var torrent : torrent_info_list)
        {
            torrent.PrintInfo();
            System.out.println("=================================");
        }

    }


}
