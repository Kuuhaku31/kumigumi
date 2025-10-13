package utils.Info;

import utils.State.StateDownload;
import utils.State.StateView;

import java.time.LocalDate;
import java.time.LocalTime;

public
class EpisodeInfo
{
    public final int ep_id;
    public final int ani_id;

    public LocalDate air_date    = null;
    public LocalTime duration    = LocalTime.MIN;
    public String    index       = "";
    public String    title       = "";
    public String    title_cn    = "";
    public String    description = "";

    public StateDownload status_download = StateDownload.NotDownloaded;
    public StateView     status_view     = StateView.NotWatched;
    public int           rating          = 0;
    public String        remark          = "";

    public
    EpisodeInfo(int ep_id, int ani_id)
    {
        this.ep_id  = ep_id;
        this.ani_id = ani_id;
    }

    public
    void PrintInfo()
    {
        String header     = "================== [" + this + "] ==================";
        int    header_len = header.length();
        IO.println(header);
        IO.println("ep_id           [集数 ID]:         " + ep_id);
        IO.println("ani_id          [番组 Bangumi ID]: " + ani_id);
        IO.println("air_date        [放送日期]:        " + air_date);
        IO.println("duration        [时长]:            " + duration);
        IO.println("index           [集数]:            " + index);
        IO.println("title           [标题]:            " + title);
        IO.println("title_cn        [中文标题]:        " + title_cn);
        IO.println("description     [概述]:\n" + description);
        for(int i = 0; i < header_len; i++) IO.print("-");
        IO.println();
        IO.println("status_download [下载状态]:        " + status_download);
        IO.println("status_view     [观看状态]:        " + status_view);
        IO.println("rating          [评分]:            " + rating);
        IO.println("remark          [备注]:            " + remark);
        for(int i = 0; i < header_len; i++) IO.print("=");
        IO.println();
    }

}