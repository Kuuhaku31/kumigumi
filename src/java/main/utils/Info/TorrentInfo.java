package utils.Info;

import java.time.LocalDateTime;

public
class TorrentInfo
{
    public String torrent_url = "";
    public int ani_id = 0;
    public LocalDateTime air_date_time;
    public String page_url = "";
    public String subtitle_group = "";
    public String title = "";
    public String description = "";
    public long size = 0;
    public String download_status = "未下载";
    public String remark = "";

    public
    void PrintInfo()
    {
        IO.println("torrent_url: " + torrent_url);
        IO.println("ani_id: " + ani_id);
        IO.println("air_date: " + air_date_time);
        IO.println("page_url: " + page_url);
        IO.println("subtitle_group: " + subtitle_group);
        IO.println("title: " + title);
        IO.println("description: " + description);
        IO.println("size: " + size);
        IO.println("download_status: " + download_status);
        IO.println("remark: " + remark);
    }

}
