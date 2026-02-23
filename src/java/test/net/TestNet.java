package net;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;

import InfoItem.InfoEpi.InfoEpi;
import InfoItem.InfoEpi.InfoEpiFetch;
import MetaData.TestMetaData;
import NetAccess.NetAccess;


public class TestNet {
    public static void main(String[] args) throws URISyntaxException, IOException {
        System.out.println("Starting TestNetAccess...");
        String url = "https://mikan.tangbai.cc/Download/20260223/fdffa65ed576c6f41c9fd581beb6147a9b0be0f5.torrent";
        var res = NetAccess.DownloadFile(url);
        System.out.println("Download result: " + res);
        OutputStream os = java.nio.file.Files.newOutputStream(java.nio.file.Paths.get("test_download.torrent"));
        os.write(res);
        os.close();
    }

    void fun1() throws URISyntaxException, IOException {
        var meta          = TestMetaData.meta_公主管弦乐;
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
}
