// Test01.java

package test;

import NetAccess.Bangumi.BangumiPageInfo;
import NetAccess.Bangumi.BangumiParser;
import NetAccess.MikanAnime.MikanParser;
import NetAccess.Net;
import utils.InfoSet;
import utils.TorrentInfo;
import utils.Translate;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public
class Tets01
{
    private
    void Test0(String[] args)
    {
        String url = args[0];
        String html_str;
        try
        {
            html_str = Net.GetHTML(url);
        }
        catch(IOException e)
        {
            throw new RuntimeException(e);
        }

        BangumiPageInfo page_info = BangumiParser.ParseBangumiHTML(html_str);
        IO.println("打印结果: ");
        page_info.animeInfo.Translate();
        for(var episode : page_info.episodeInfoList)
        {
            episode.Translate();
        }

        page_info.PrintInfo();
    }

    private
    void Test1()
    {
        Translate.PrintMap();
        String key = "种子描述";
        String res = Translate.TranslateKey(key);
        IO.println(key + " -> " + res);
    }

    private
    void Test2(String url)
    {
        IO.println("Test2");
        IO.println("url: " + url);

        String html_str;
        try
        {
            html_str = Net.GetHTML(url);
        }
        catch(IOException e)
        {
            throw new RuntimeException(e);
        }

        List<TorrentInfo> info_list = MikanParser.ParseMikanRssXML("1234", html_str);

        for(var torrent : info_list)
        {
            torrent.Translate();
            torrent.PrintInfo();
            IO.println("=================================");
        }
    }

    private
    void Test3(String bangumi_url, String mikan_url)
    {
        InfoSet info_set = NetAccess.InfoGetter.GetInfo(bangumi_url, mikan_url);
        info_set.Translate();
        info_set.PrintInfo();
    }

    void main(String[] args)
    {
        IO.println(Arrays.toString(args));
        // Test0(args);
        // Test1();
        // Test2(args[1]);
        Test3(args[0], args[1]);
    }
}
