package utils.Info;

import java.time.LocalDate;
import java.time.LocalTime;

public
class EpisodeInfo
{
    public int ani_id = 0;
    public int ep_id = 0;
    public LocalDate air_date = null;
    public String index = "";
    public String title = "";
    public String title_cn = "";
    public LocalTime duration = LocalTime.MIN;
    public int rating = 0;
    public String download_status = "";
    public String view_status = "";
    public String remark = "";

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