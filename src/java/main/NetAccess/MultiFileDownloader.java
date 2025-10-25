package NetAccess;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public
class MultiFileDownloader
{

    private static final int THREAD_COUNT = 8; // 最大并发下载数


    static
    void main() throws InterruptedException
    {
        String path_str = "C:/Users/kuuhaku-kzs/Downloads/dt/";

        System.setProperty("java.net.useSystemProxies", "true"); // 启用系统代理支持

        // 示例 URL 列表
        List<String> urls = new ArrayList<>();
        urls.add("https://mikanani.me/Download/20250414/0f8ba83fc8996d6946dfee1de2019052a9623150.torrent");
        urls.add("https://mikanani.me/Download/20250424/d5d40ef07602f5e022e9ec7bfb8344ec20fee4e9.torrent");
        urls.add("https://mikanani.me/Download/20250427/ 3210d7540d3ec4a0d45e1040e069d7e933b56949.torrent");


        var failed_urls = DownloadAll(urls, Path.of(path_str));
        System.out.println(failed_urls);
    }


    /**
     * 下载所有 URL
     * <p>
     * 返回下载失败的 URL 列表
     */
    public static
    List<String> DownloadAll(List<String> urls, Path downloadDir) throws InterruptedException
    {
        var client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.ALWAYS) // 自动跟随重定向
            .connectTimeout(Duration.ofSeconds(10))      // 设置连接超时
            .build();

        var executor   = Executors.newFixedThreadPool(THREAD_COUNT); // 固定大小线程池
        var failedUrls = new ConcurrentLinkedQueue<String>();        // 用于保存下载失败的 URL

        // 提交下载任务
        for(String url : urls)
        {
            executor.submit(() ->
            {
                try
                {
                    // 构建 URI 和请求
                    var uri        = URI.create(url);
                    var fileName   = Paths.get(uri.getPath()).getFileName().toString();
                    var targetPath = downloadDir.resolve(fileName);

                    var request  = HttpRequest.newBuilder(uri).GET().build();
                    var response = client.send(request, HttpResponse.BodyHandlers.ofFile(targetPath));

                    if(response.statusCode() != 200)
                    {
                        System.err.println("下载失败：" + url + " 状态码：" + response.statusCode());
                        failedUrls.add(url);
                    }
                    else
                    {
                        System.out.println("✅ 下载成功：" + fileName);
                    }
                }
                catch(Exception e)
                {
                    System.err.println("❌ 下载异常：" + url + " → " + e.getMessage());
                    failedUrls.add(url);
                }
            });
        }

        // 等待所有任务完成
        executor.shutdown();
        if(executor.awaitTermination(1, TimeUnit.HOURS)) System.out.println("所有下载任务已完成");
        else System.out.println("下载任务超时");

        // 返回失败列表
        return new ArrayList<>(failedUrls);
    }
}
