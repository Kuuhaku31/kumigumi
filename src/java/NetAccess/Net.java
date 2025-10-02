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
        Document doc = Jsoup.connect(url).get();
        return doc.html();
    }
}
