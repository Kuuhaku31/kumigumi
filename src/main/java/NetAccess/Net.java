// Net.java

package NetAccess;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

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
}
