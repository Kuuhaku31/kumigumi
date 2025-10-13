package utils.Info;

import java.time.LocalDate;

public
class AnimeInfo
{
    public final int ani_id;

    public LocalDate air_date          = null;
    public String    title             = "";
    public String    title_cn          = "";
    public String    aliases           = "";
    public int       episode_count     = 0;
    public String    url_official_site = "";
    public String    url_cover         = "";

    public String url_rss           = "";
    public int    pre_view_rating   = 0;
    public int    after_view_rating = 0;
    public String remark            = "";


    public
    AnimeInfo(int ani_id)
    {
        this.ani_id = ani_id;
    }


    public
    void PrintInfo()
    {
        String header     = "================== [" + this + "] ==================";
        int    header_len = header.length();
        IO.println(header);
        IO.println("id                [番组 Bangumi ID]: " + ani_id);
        IO.println("air_date          [首播日期]:        " + air_date);
        IO.println("title             [标题]:            " + title);
        IO.println("title_cn          [中文标题]:        " + title_cn);
        IO.println("aliases           [别名]:            " + aliases);
        IO.println("episode_count     [总集数]:          " + episode_count);
        IO.println("url_official_site [官方网站]:        " + url_official_site);
        IO.println("url_cover         [封面图片]:        " + url_cover);
        for(int i = 0; i < header_len; i++) IO.print("-");
        IO.println();
        IO.println("url_rss           [RSS 订阅]:        " + url_rss);
        IO.println("pre_view_rating   [观看前评分]:      " + pre_view_rating);
        IO.println("after_view_rating [观看后评分]:      " + after_view_rating);
        IO.println("remark            [备注]:            " + remark);
        for(int i = 0; i < header_len; i++) IO.print("=");
        IO.println();

    }


}