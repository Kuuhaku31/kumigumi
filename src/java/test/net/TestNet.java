package net;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;

import InfoItem.InfoEpi.InfoEpi;
import InfoItem.InfoEpi.InfoEpiFetch;
import MetaData.ARGS;
import NetAccess.NetAccess;


public class TestNet {
    public static void main(String[] args) throws URISyntaxException, IOException {
        fun0();
    }

    static void fun0() throws URISyntaxException, IOException {
        var meta = ARGS.meta_公主管弦乐;
        var tor_info_list = NetAccess.FetchAnimeTorrentInfo(meta.url_rss);
        
        // 保存到文件
        try(OutputStream os = java.nio.file.Files.newOutputStream(java.nio.file.Paths.get("test_torrent_info.tmp"))) {
            for(var tor_info : tor_info_list) {
                os.write(tor_info.toString().getBytes());
                os.write("\n".getBytes());
            }
        }
    }

    static void fun1() throws URISyntaxException, IOException {
        var meta          = ARGS.meta_公主管弦乐;
        var epi_info_list = NetAccess.FetchEpisodeInfo(meta.ANI_ID);
        for(var map : epi_info_list) {
            for(var entry : map.entrySet()) {
                // var key = entry.getKey();
                var value = entry.getValue();

                if(value != null && value.equals("1570388")) {
                    System.out.println("Found episode with ID 1570388");
                }
            }
        }

        for(var epi : epi_info_list) {
            var infoItem = new InfoEpi(epi);
            System.out.println(infoItem);

            var infoItem2 = new InfoEpiFetch(epi);
            System.out.println(infoItem2);
        }
    }

    void fun2() throws IOException, URISyntaxException {
        System.out.println("Starting TestNetAccess...");
        String url = "https://mikan.tangbai.cc/Download/20260223/fdffa65ed576c6f41c9fd581beb6147a9b0be0f5.torrent";
        var    res = NetAccess.DownloadFile(url);
        System.out.println("Download result: " + res);
        OutputStream os = java.nio.file.Files.newOutputStream(java.nio.file.Paths.get("test_download.torrent"));
        os.write(res);
        os.close();
    }
}
