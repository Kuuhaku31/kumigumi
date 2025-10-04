// InfoGetter.java

package NetAccess;

import NetAccess.Bangumi.BangumiParser;
import NetAccess.MikanAnime.MikanParser;
import utils.InfoSet;

public
class InfoGetter
{
    public static
    InfoSet GetInfo(String bangumi_url, String mikan_url)
    {
        String bangumi_html_str;
        String mikan_html_str;
        try
        {
            bangumi_html_str = Net.GetHTML(bangumi_url);
            mikan_html_str = Net.GetHTML(mikan_url);
        }
        catch(Exception e)
        {
            throw new RuntimeException(e);
        }

        var bangumi_page_info = BangumiParser.ParseBangumiHTML(bangumi_html_str);
        var mikan_torrent_list = MikanParser.ParseMikanRssXML(bangumi_url, mikan_html_str);

        InfoSet info_set = new InfoSet();
        info_set.anime_info = bangumi_page_info.animeInfo;
        info_set.episode_info_list = bangumi_page_info.episodeInfoList;
        info_set.torrent_info_list = mikan_torrent_list;

        return info_set;
    }
}
