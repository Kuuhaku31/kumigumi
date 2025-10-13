package utils.Info;

import java.time.LocalDate;

public
class AnimeInfo
{
    public int ani_id = 0;
    public LocalDate air_date;
    public String title = "";
    public String title_cn = "";
    public String aliases = "";
    public int episode_count = 0;
    public String official_site_url = "";
    public String cover_url = "";

    public int pre_view_rating = 0;
    public int after_view_rating = 0;
    public String rss_url = "";
    public String remark = "";

    public
    void PrintInfo()
    {
        IO.println("ani_id: " + ani_id);
        IO.println("air_date: " + air_date);
        IO.println("title: " + title);
        IO.println("title_cn: " + title_cn);
        IO.println("aliases: " + aliases);
        IO.println("episode_count: " + episode_count);
        IO.println("official_site_url: " + official_site_url);
        IO.println("cover_url: " + cover_url);
        IO.println("pre_view_rating: " + pre_view_rating);
        IO.println("after_view_rating: " + after_view_rating);
        IO.println("rss_url: " + rss_url);
        IO.println("remark: " + remark);
    }
    

}