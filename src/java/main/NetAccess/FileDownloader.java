package NetAccess;


import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;

public
class FileDownloader
{
    private static final
    HttpClient client = HttpClient.newBuilder()
        .followRedirects(HttpClient.Redirect.ALWAYS)
        .build();

    public static
    String Get(String url_str) throws URISyntaxException, IOException, InterruptedException
    {
        // 构建请求
        var request = HttpRequest.newBuilder()
            .uri(new URI(url_str))
            .GET()
            .header("User-Agent", "Mozilla/5.0")
            .build();

        // 发送请求并接收响应
        var response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // 检查 HTTP 状态码
        if(response.statusCode() != 200) throw new IOException("HTTP 错误码: " + response.statusCode());

        return response.body();
    }

    public static
    void DownloadFile(String url, Path savePath) throws IOException, InterruptedException
    {
        // 创建请求
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("User-Agent", "Mozilla/5.0")
            .GET()
            .build();

        // 发送请求并获取字节数组响应
        HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());

        // 检查状态码
        if(response.statusCode() != 200)
            throw new IOException("HTTP 错误码: " + response.statusCode());

        // 确保目录存在
        Files.createDirectories(savePath.getParent());

        // 写入文件
        try(FileOutputStream out = new FileOutputStream(savePath.toFile()))
        {
            out.write(response.body());
        }

        System.out.println("下载完成: " + savePath);
    }
}