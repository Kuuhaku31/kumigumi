package utils;

import java.time.LocalDate;
import java.time.LocalTime;

public
class EpisodeInfo
{
    public int ani_id;
    public int ep_id;
    public LocalDate air_date;
    public String index;
    public String title;
    public String title_cn;
    public LocalTime duration;
    public int rating;
    public String download_status;
    public String view_status;
    public String remark;

    public
    void PrintInfo()
    {
        IO.println("ani_id: " + ani_id);
        IO.println("ep_id: " + ep_id);
        IO.println("air_date: " + air_date);
        IO.println("index: " + index);
        IO.println("title: " + title);
        IO.println("title_cn: " + title_cn);
        IO.println("duration: " + duration);
        IO.println("rating: " + rating);
        IO.println("download_status: " + download_status);
        IO.println("view_status: " + view_status);
        IO.println("remark: " + remark);
    }
}