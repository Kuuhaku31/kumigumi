package utils.Info;

import java.time.LocalDateTime;

public
class TorrentInfo
{
    public final String tor_url;

    public int           ani_id         = 0;
    public LocalDateTime air_datetime   = null;
    public long          size           = 0;
    public String        url_page       = "";
    public String        title          = "";
    public String        subtitle_group = "";
    public String        description    = "";

    public utils.State.StateDownload status_download = utils.State.StateDownload.NotDownloaded;
    public String                    remark          = "";

    public
    TorrentInfo(String tor_url)
    {
        this.tor_url = tor_url;
    }

    public
    void PrintInfo()
    {
        String header     = "================== [" + this + "] ==================";
        int    header_len = header.length();
        IO.println(header);
        IO.println("tor_url         [种子链接]:        " + tor_url);
        IO.println("ani_id          [番组 Bangumi ID]: " + ani_id);
        IO.println("air_datetime    [发布时间]:        " + air_datetime);
        IO.println("size            [大小]:            " + size);
        IO.println("url_page        [网页链接]:        " + url_page);
        IO.println("title           [标题]:            " + title);
        IO.println("subtitle_group  [字幕组]:          " + subtitle_group);
        IO.println("description     [描述]:\n" + description);
        for(int i = 0; i < header_len; i++) IO.print("-");
        IO.println();
        IO.println("status_download [下载状态]:        " + status_download);
        IO.println("remark          [备注]:            " + remark);
        for(int i = 0; i < header_len; i++) IO.print("=");
        IO.println();
    }
}
