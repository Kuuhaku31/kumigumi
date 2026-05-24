package Database;

import java.util.List;

public record TorrentDownloader(String TOR_HASH, List<String> url_download_list) {
}
