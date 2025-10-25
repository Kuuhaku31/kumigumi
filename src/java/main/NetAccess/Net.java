// Net.java

package NetAccess;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;

public
class Net
{
    static public
    String GetHTML(String url) throws IOException
    {
        Document doc = Jsoup.connect(url).get();
        return doc.html();
    }


    static public
    String Get(String url_str) throws URISyntaxException, IOException
    {
        URL               url  = new URI(url_str).toURL();                  // 创建URL对象
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();  // 打开连接
        conn.setRequestMethod("GET");                                       // 设置请求方法
        conn.setRequestProperty("User-Agent", "Mozilla/5.0");               // 添加 User-Agent

        // 读取响应
        BufferedReader in       = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder  response = new StringBuilder();

        // 逐行读取响应内容
        String input_line;
        while((input_line = in.readLine()) != null) response.append(input_line);

        in.close();

        return response.toString();
    }

    /**
     * 下载文件
     * <p>
     * 返回 null 表示成功，返回 url 表示失败
     */
    public static
    String DownloadFile(HttpClient client, String file_url, String save_path, String file_name_str)
    {
        try
        {
            var request  = HttpRequest.newBuilder().uri(URI.create(file_url)).GET().build(); // 构建请求
            var response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());  // 发送请求

            if(response.statusCode() == 200) // 将响应体保存为文件
            {
                Files.createDirectories(Paths.get(save_path)); // 创建文件夹
                Files.copy(response.body(), Paths.get(save_path + file_name_str), StandardCopyOption.REPLACE_EXISTING);

                return null;
            }
            else return file_url;
        }
        catch(IOException | InterruptedException e)
        {
            return file_url;
        }
    }

    static
    void main()
    {
        System.setProperty("java.net.useSystemProxies", "true"); // 启用系统代理支持

        HttpClient client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.ALWAYS) // 自动跟随重定向
            .connectTimeout(Duration.ofSeconds(10))
            .build();

        String path_str = "C:/Users/kuuhaku-kzs/Downloads/dt/";
        String url      = "https://mikanani.me/Download/20250414/0f8ba83fc8996d6946dfee1de2019052a9623150.torrent";
        IO.println(DownloadFile(client, url, path_str, "a.torrent"));
    }

}
