package test;

import Bangumi.BangumiPageInfo;
import Bangumi.BangumiParser;
import NetAccess.Net;
import utils.Translate;

import java.io.IOException;
import java.util.Arrays;

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

        BangumiPageInfo page_info = BangumiParser.ParseBangumiHtml(html_str);
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

    void main(String[] args)
    {
        IO.println(Arrays.toString(args));
        Test0(args);
        // Test1();
    }
}
