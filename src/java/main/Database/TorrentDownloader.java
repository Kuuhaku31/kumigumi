package Database;

import java.util.List;

public record TorrentDownloader(String TOR_HASH, List<String> url_download_list) {
    public List<String> getUrlList() {
        return url_download_list;
    }

    @Override
    public String toString() {
        return String.format("TorrentDownloader{TOR_HASH='%s', url_download_list=%s}", TOR_HASH, url_download_list);
    }
}
