package test;

import Bangumi.BangumiPageInfo;
import Bangumi.BangumiParser;
import NetAccess.Net;

import java.io.IOException;
import java.util.Arrays;

public
class Tets01
{
    void main(String[] args)
    {
        IO.println(Arrays.toString(args));
        String url = args[0];

        String html_str = null;
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
        page_info.PrintInfo();
    }
}
