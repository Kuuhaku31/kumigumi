package FetchTask;

import java.io.IOException;
import java.net.URISyntaxException;

import NetAccess.NetAccess;
import Util.TorrentMetaUtil;

public class Main {
    static String tor_hash = "54491ff421a4df92ed295b9708a0888d13f2f63f";
    static String tor_url  = "https://mikan.tangbai.cc/Download/20260220/54491ff421a4df92ed295b9708a0888d13f2f63f.torrent";

    public static void main(String[] args) throws URISyntaxException, IOException {
        var torInfoByte = NetAccess.DownloadFile(tor_url);
        var meta = TorrentMetaUtil.extractMeta(torInfoByte);

        System.out.println("Extracted metadata:");
        System.out.println("File Name: " + meta.fileName);
        System.out.println("File Size: " + meta.fileSize);
    }
}
