package utils.task;


import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;

public
class TaskDT extends KGTask
{
    private final Path   download_dir;
    private final String torrent_url;

    public
    TaskDT(Path download_dir, String torrent_url)
    {
        this.download_dir = download_dir;
        this.torrent_url  = torrent_url;
    }

    @Override
    public
    void run()
    {
        var client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.ALWAYS) // 自动跟随重定向
            .connectTimeout(Duration.ofSeconds(60))      // 设置连接超时
            .build();

        // 构建 URI 和请求
        var uri       = URI.create(torrent_url);
        var file_name = Paths.get(uri.getPath()).getFileName().toString();

        var request = HttpRequest.newBuilder(uri).GET().build();

        HttpResponse<Path> response = null;
        try { response = client.send(request, HttpResponse.BodyHandlers.ofFile(download_dir.resolve(file_name))); }
        catch(IOException | InterruptedException e) { System.err.println(e.getMessage()); }

        if(response == null || response.statusCode() != 200)
        {
            System.err.println("种子下载失败: " + torrent_url);
        }
    }
}
