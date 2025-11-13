import static NetAccess.FileDownloader.DownloadFile;
import static NetAccess.FileDownloader.Get;

void main() throws URISyntaxException, IOException, InterruptedException
{
    System.out.println("Test Download");

    System.setProperty("java.net.useSystemProxies", "true"); // 设置全局代理


    var dt_path = Path.of("D:/Downloads/dt/a");
    // if(Files.notExists(dt_path))
    // {
    //     try { Files.createDirectories(dt_path); }
    //     catch(IOException e) { System.err.println("无法创建下载路径: " + e.getMessage()); }
    // }

    var url  = "https://mikanani.me/Download/20251020/cd08b6d4a7e90fb8368a7acdbeb72b7344a6ada1.torrent";
    var url1 = "https://api.bgm.tv/v0/subjects/507634";

    var res = Get(url1);

    System.out.println(res);

    DownloadFile(url, dt_path);
}
