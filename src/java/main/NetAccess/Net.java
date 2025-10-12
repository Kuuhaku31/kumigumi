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

public
class Net
{
    static public
    String GetHTML(String url) throws IOException
    {
        // Document doc = Jsoup.connect(url).get();
        Document doc = Jsoup.connect(url)
            .proxy("127.0.0.1", 10809) // HTTP 代理地址和端口
            .timeout(10000) // 设置超时时间
            .get();
        return doc.html();
    }

    static public
    String Get(String url_str) throws URISyntaxException, IOException
    {
        URL url = new URI(url_str).toURL();                                 // 创建URL对象
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();  // 打开连接
        conn.setRequestMethod("GET");                                       // 设置请求方法
        conn.setRequestProperty("User-Agent", "Mozilla/5.0");               // 添加 User-Agent

        // 读取响应
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder response = new StringBuilder();

        // 逐行读取响应内容
        String input_line;
        while((input_line = in.readLine()) != null) response.append(input_line);

        in.close();

        return response.toString();
    }
}
