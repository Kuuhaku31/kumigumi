package utils;

import java.time.LocalDate;

public
class AnimeInfo
{
    public int bangumi_id;
    public LocalDate air_date;
    public String title;
    public String title_cn;
    public String aliases;
    public int episode_count;
    public String official_site_url;
    public String cover_url;

    public int pre_view_rating;
    public int after_view_rating;
    public String rss_url;
    public String remark;

    public
    void PrintInfo()
    {
        IO.println("bangumi_id: " + bangumi_id);
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