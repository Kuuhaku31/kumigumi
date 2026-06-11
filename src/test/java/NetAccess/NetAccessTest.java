package NetAccess;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class NetAccessTest {

    @Test
    void downloadsFileBytesFromUrl() throws Exception {

        var body   = "torrent bytes".getBytes(StandardCharsets.UTF_8);
        var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);

        server.createContext("/file.torrent", exchange -> {
            exchange.sendResponseHeaders(200, body.length);
            try(var out = exchange.getResponseBody()) {
                out.write(body);
            }
        });

        server.start();
        try {
            var url = "http://127.0.0.1:" + server.getAddress().getPort() + "/file.torrent";
            assertArrayEquals(body, NetAccess.DownloadFile(url));
        } finally {
            server.stop(0);
        }
    }

    @Test
    void rejectsUnsupportedRssSource() {
        assertThrows(IOException.class, () -> NetAccess.FetchTorrentPageInfoSet("https://example.com/rss.xml"));
    }

    @Test
    void rejectsNullBangumiAnimeIdBeforeNetworkAccess() {
        assertThrows(IllegalArgumentException.class, () -> NetAccess.FetchAnimeInfo(null));
        assertThrows(IllegalArgumentException.class, () -> NetAccess.FetchEpisodeInfoSet(null));
    }
}
