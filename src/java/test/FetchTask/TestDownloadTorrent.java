package FetchTask;

import java.io.IOException;

import NetAccess.NetAccess;
import Util.TorrentMetaUtil;

public class TestDownloadTorrent {
    public static void main(String[] args) {
        // var url = "https://mikan.tangbai.cc/Download/20260119/6cc64948f446cb3de0a33187ef41fe65d1840d45.torrent";
        var url2 = "https://mikan.tangbai.cc/Download/20260202/42cae1cb92b2903eae8223b74a12bdfac0de9767.torrent";

        byte[] file_bin = null;

        try {
            file_bin = NetAccess.DownloadFile(url2);
        } catch(IOException | java.net.URISyntaxException e) {
            System.err.println("Error downloading file: " + e.getMessage());
            return;
        }

        System.out.println("Downloaded file size: " + file_bin.length + " bytes");

        var res = TorrentMetaUtil.extractMeta(file_bin);

        System.out.println("Extracted metadata:");
        System.out.println("File Name: " + res.fileName);
        System.out.println("File Size: " + res.fileSize + " bytes");
    }
}
